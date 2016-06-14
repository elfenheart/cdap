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

package co.cask.cdap.common.conf;

/**
 * Configuration parameters for Kafka server.
 */
public final class KafkaConstants {

  /**
   * Keys for configuration parameters.
   */
  public static final class ConfigKeys {

    public static final String PORT_CONFIG = "kafka.bind.port";
    public static final String NUM_PARTITIONS_CONFIG = "kafka.num.partitions";
    public static final String LOG_DIR_CONFIG = "kafka.log.dir";
    public static final String HOSTNAME_CONFIG = "kafka.bind.address";
    public static final String ZOOKEEPER_NAMESPACE_CONFIG = "kafka.zookeeper.namespace";
    public static final String REPLICATION_FACTOR = "kafka.default.replication.factor";
    public static final String RETENTION_HOURS_CONFIG = "kafka.log.retention.hours";
    public static final String BUFFER_BYTES_SEND_CONFIG = "kafka.socket.send.buffer.bytes";
    public static final String BUFFER_BYTES_RECEIVE_CONFIG = "kafka.socket.receive.buffer.bytes";
    public static final String MAX_BYTES_CONFIG = "kafka.socket.request.max.bytes";
    public static final String INTERVAL_MESSAGES_CONFIG = "kafka.log.flush.interval.messages";
    public static final String INTERVAL_MS_CONFIG = "kafka.log.flush.interval.ms";
    public static final String SEGEMENT_BYTES_CONFIG = "kafka.log.segment.bytes";
    public static final String ZOOKEEPER_CONNECTION_TIMEOUT_CONFIG = "kafka.zookeeper.connection.timeout.ms";
  }

  public static final int DEFAULT_NUM_PARTITIONS = 10;
  public static final int DEFAULT_REPLICATION_FACTOR = 1;
  public static final int DEFAULT_RETENTION_HOURS = 168;
  public static final int DEFAULT_BUFFER_BYTES = 102400;
  public static final int DEFAULT_SOCKET_MAX_BYTES = 104857600;
  public static final long DEFAULT_INTERVAL_MESSAGE_SIZE = 9223372036854775807L;
  public static final int DEFAULT_LOG_FILE_SIZE = 1073741824;

}
