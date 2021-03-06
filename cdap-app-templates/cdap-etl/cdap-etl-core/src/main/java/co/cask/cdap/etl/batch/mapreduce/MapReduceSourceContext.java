/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.etl.batch.mapreduce;

import co.cask.cdap.api.data.batch.Input;
import co.cask.cdap.api.data.batch.InputFormatProvider;
import co.cask.cdap.api.data.batch.Split;
import co.cask.cdap.api.data.format.FormatSpecification;
import co.cask.cdap.api.data.stream.StreamBatchReadable;
import co.cask.cdap.api.mapreduce.MapReduceContext;
import co.cask.cdap.api.metrics.Metrics;
import co.cask.cdap.etl.api.LookupProvider;
import co.cask.cdap.etl.api.batch.BatchSourceContext;
import co.cask.cdap.etl.common.ExternalDatasets;
import co.cask.cdap.etl.log.LogContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * MapReduce Source Context.
 */
public class MapReduceSourceContext extends MapReduceBatchContext implements BatchSourceContext {

  public MapReduceSourceContext(MapReduceContext context, Metrics metrics, LookupProvider lookup, String stageName,
                                Map<String, String> runtimeArgs) {
    super(context, metrics, lookup, stageName, runtimeArgs);
  }

  @Override
  public void setInput(final StreamBatchReadable stream) {
    LogContext.runWithoutLoggingUnchecked(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        FormatSpecification formatSpec = stream.getFormatSpecification();
        if (formatSpec == null) {
          mrContext.addInput(Input.ofStream(stream.getStreamName(), stream.getStartTime(), stream.getEndTime()));
        } else {
          mrContext.addInput(Input.ofStream(stream.getStreamName(), stream.getStartTime(),
                                            stream.getEndTime(), formatSpec));
        }
        return null;
      }
    });
  }

  @Override
  public void setInput(final String datasetName) {
    LogContext.runWithoutLoggingUnchecked(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        mrContext.addInput(Input.ofDataset(datasetName));
        return null;
      }
    });
  }

  @Override
  public void setInput(final String datasetName, final Map<String, String> arguments) {
    LogContext.runWithoutLoggingUnchecked(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        mrContext.addInput(Input.ofDataset(datasetName, arguments));
        return null;
      }
    });
  }

  @Override
  public void setInput(final String datasetName, final List<Split> splits) {
    LogContext.runWithoutLoggingUnchecked(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        mrContext.addInput(Input.ofDataset(datasetName, splits));
        return null;
      }
    });
  }

  @Override
  public void setInput(final String datasetName, final Map<String, String> arguments, final List<Split> splits) {
    LogContext.runWithoutLoggingUnchecked(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        mrContext.addInput(Input.ofDataset(datasetName, arguments, splits));
        return null;
      }
    });
  }

  @Override
  public void setInput(final InputFormatProvider inputFormatProvider) {
    LogContext.runWithoutLoggingUnchecked(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        mrContext.addInput(Input.of(inputFormatProvider.getInputFormatClassName(), inputFormatProvider));
        return null;
      }
    });
  }

  @Override
  public void setInput(final Input input) {
    LogContext.runWithoutLoggingUnchecked(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        Input trackableInput =  ExternalDatasets.makeTrackable(mrContext.getAdmin(), input);
        mrContext.addInput(trackableInput);
        return null;
      }
    });
  }
}
