/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.kafka.run;

import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.conf.KafkaConstants;
import co.cask.cdap.common.runtime.DaemonMain;
import co.cask.cdap.common.utils.Networks;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.Service;
import org.apache.twill.internal.kafka.EmbeddedKafkaServer;
import org.apache.twill.zookeeper.ZKClientService;
import org.apache.twill.zookeeper.ZKOperations;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Runs embedded Kafka server.
 */
public class KafkaServerMain extends DaemonMain {
  private static final Logger LOG = LoggerFactory.getLogger(KafkaServerMain.class);

  private Properties kafkaProperties;
  private EmbeddedKafkaServer kafkaServer;

  public static void main(String [] args) throws Exception {
    new KafkaServerMain().doMain(args);
  }

  @Override
  public void init(String[] args) {
    CConfiguration cConf = CConfiguration.create();
    String zkConnectStr = cConf.get(Constants.Zookeeper.QUORUM);
    String zkNamespace = cConf.get(KafkaConstants.ConfigKeys.ZOOKEEPER_NAMESPACE_CONFIG);

    int port = cConf.getInt(KafkaConstants.ConfigKeys.PORT_CONFIG, -1);
    String hostname = cConf.get(KafkaConstants.ConfigKeys.HOSTNAME_CONFIG);

    InetAddress address = Networks.resolve(hostname, new InetSocketAddress("localhost", 0).getAddress());
    if (address.isAnyLocalAddress()) {
      try {
        address = InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
        throw Throwables.propagate(e);
      }
    }
    if (address.isLoopbackAddress()) {
      LOG.warn("Binding to loopback address!");
    }
    hostname = address.getCanonicalHostName();

    int numPartitions = cConf.getInt(KafkaConstants.ConfigKeys.NUM_PARTITIONS_CONFIG,
                                     KafkaConstants.DEFAULT_NUM_PARTITIONS);
    String logDir = cConf.get(KafkaConstants.ConfigKeys.LOG_DIR_CONFIG);

    int replicationFactor = cConf.getInt(KafkaConstants.ConfigKeys.REPLICATION_FACTOR,
                                         KafkaConstants.DEFAULT_REPLICATION_FACTOR);
    LOG.info("Using replication factor {}", replicationFactor);

    int retentionHours = cConf.getInt(KafkaConstants.ConfigKeys.RETENTION_HOURS_CONFIG,
                                      KafkaConstants.DEFAULT_RETENTION_HOURS);

    int bufferSendBytes = cConf.getInt(KafkaConstants.ConfigKeys.BUFFER_BYTES_SEND_CONFIG,
                                       KafkaConstants.DEFAULT_BUFFER_BYTES);
    int bufferReceiveBytes = cConf.getInt(KafkaConstants.ConfigKeys.BUFFER_BYTES_RECEIVE_CONFIG,
                                          KafkaConstants.DEFAULT_BUFFER_BYTES);

    int socketRequestMaxBytes = cConf.getInt(KafkaConstants.ConfigKeys.MAX_BYTES_CONFIG,
                                             KafkaConstants.DEFAULT_SOCKET_MAX_BYTES);

    long intervalMessageSize = cConf.getLong(KafkaConstants.ConfigKeys.INTERVAL_MESSAGES_CONFIG,
                                             KafkaConstants.DEFAULT_INTERVAL_MESSAGE_SIZE);

    long messagePersistTime = cConf.getLong(KafkaConstants.ConfigKeys.INTERVAL_MS_CONFIG);

    int logFileSize = cConf.getInt(KafkaConstants.ConfigKeys.SEGEMENT_BYTES_CONFIG,
                                   KafkaConstants.DEFAULT_LOG_FILE_SIZE);

    int zookeeperTimeout = cConf.getInt(KafkaConstants.ConfigKeys.ZOOKEEPER_CONNECTION_TIMEOUT_CONFIG);

    if (zkNamespace != null) {
      ZKClientService client = ZKClientService.Builder.of(zkConnectStr).build();
      try {
        client.startAndWait();

        String path = "/" + zkNamespace;
        LOG.info(String.format("Creating zookeeper namespace %s", path));

        ZKOperations.ignoreError(
          client.create(path, null, CreateMode.PERSISTENT),
          KeeperException.NodeExistsException.class, path).get();

        client.stopAndWait();
        zkConnectStr = String.format("%s/%s", zkConnectStr, zkNamespace);
      } catch (Exception e) {
        throw Throwables.propagate(e);
      } finally {
        client.stopAndWait();
      }
    }

    int brokerId = generateBrokerId(address);
    LOG.info(String.format("Initializing server with broker id %d", brokerId));

    kafkaProperties = generateKafkaConfig(brokerId, zkConnectStr, hostname, port, numPartitions,
                                          replicationFactor, logDir, retentionHours, bufferSendBytes,
                                          bufferReceiveBytes, socketRequestMaxBytes, intervalMessageSize,
                                          messagePersistTime, logFileSize, zookeeperTimeout);
  }

  @Override
  public void start() {
    LOG.info("Starting embedded kafka server...");

    kafkaServer = new EmbeddedKafkaServer(kafkaProperties);
    Service.State state = kafkaServer.startAndWait();

    if (state != Service.State.RUNNING) {
      throw new  IllegalStateException("Kafka server has not started... terminating.");
    }

    LOG.info("Embedded kafka server started successfully.");
  }

  @Override
  public void stop() {
    LOG.info("Stopping embedded kafka server...");
    if (kafkaServer != null && kafkaServer.isRunning()) {
      kafkaServer.stopAndWait();
    }
  }

  @Override
  public void destroy() {
    // Nothing to do
  }

  private Properties generateKafkaConfig(int brokerId, String zkConnectStr, String hostname, int port,
                                         int numPartitions, int replicationFactor, String logDir, int retentionHours,
                                         int bufferSendBytes, int bufferReceiveBytes, int socketRequestMaxBytes,
                                         long intervalMessageSize, long messagePersistTime, int logFileSize,
                                         int zookeeperTimeout) {
    Preconditions.checkState(port > 0, "Port number is invalid.");
    Preconditions.checkState(numPartitions > 0, "Num partitions should be greater than zero.");

    Properties prop = new Properties();
    prop.setProperty("broker.id", Integer.toString(brokerId));
    if (hostname != null) {
      prop.setProperty("host.name", hostname);
    }
    prop.setProperty("port", Integer.toString(port));
    prop.setProperty("socket.send.buffer.bytes", Integer.toString(bufferSendBytes));
    prop.setProperty("socket.receive.buffer.bytes", Integer.toString(bufferReceiveBytes));
    prop.setProperty("socket.request.max.bytes", Integer.toString(socketRequestMaxBytes));
    prop.setProperty("log.dir", logDir);
    prop.setProperty("num.partitions", Integer.toString(numPartitions));
    prop.setProperty("log.retention.hours", Integer.toString(retentionHours));
    prop.setProperty("log.flush.interval.messages", Long.toString(intervalMessageSize));
    prop.setProperty("log.flush.interval.ms", Long.toString(messagePersistTime));
    prop.setProperty("log.segment.bytes", Integer.toString(logFileSize));
    prop.setProperty("zookeeper.connect", zkConnectStr);
    prop.setProperty("zookeeper.connection.timeout.ms", Integer.toString(zookeeperTimeout));
    prop.setProperty("default.replication.factor", Integer.toString(replicationFactor));
    return prop;
  }

  private static int generateBrokerId(InetAddress address) {
    LOG.info("Generating broker ID with address {}", address);
    try {
      return Math.abs(InetAddresses.coerceToInteger(address));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
