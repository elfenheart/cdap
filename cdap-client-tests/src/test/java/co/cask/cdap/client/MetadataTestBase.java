/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.client;

import co.cask.cdap.client.common.ClientTestBase;
import co.cask.cdap.common.NotFoundException;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.artifact.ArtifactRange;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.proto.metadata.MetadataRecord;
import co.cask.cdap.proto.metadata.MetadataScope;
import co.cask.cdap.proto.metadata.MetadataSearchResultRecord;
import co.cask.cdap.proto.metadata.MetadataSearchTargetType;
import co.cask.cdap.proto.metadata.lineage.CollapseType;
import co.cask.cdap.proto.metadata.lineage.LineageRecord;
import com.google.common.collect.Iterators;
import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Before;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;
import javax.annotation.Nullable;

/**
 * Base class for metadata tests.
 */
public abstract class MetadataTestBase extends ClientTestBase {

  protected static final NamespaceId TEST_NAMESPACE1 = new NamespaceId("testnamespace1");

  private MetadataClient metadataClient;
  private LineageClient lineageClient;
  protected ArtifactClient artifactClient;
  protected NamespaceClient namespaceClient;
  protected ApplicationClient appClient;
  protected ProgramClient programClient;
  protected StreamClient streamClient;
  protected StreamViewClient streamViewClient;
  protected DatasetClient datasetClient;

  @Before
  public void beforeTest() throws IOException {
    metadataClient = new MetadataClient(getClientConfig());
    lineageClient = new LineageClient(getClientConfig());
    artifactClient = new ArtifactClient(getClientConfig());
    namespaceClient = new NamespaceClient(getClientConfig());
    appClient = new ApplicationClient(getClientConfig());
    programClient = new ProgramClient(getClientConfig());
    streamClient = new StreamClient(getClientConfig());
    streamViewClient = new StreamViewClient(getClientConfig());
    datasetClient = new DatasetClient(getClientConfig());
  }

  protected void addAppArtifact(Id.Artifact artifactId, Class<?> cls) throws Exception {
    artifactClient.add(artifactId, null, Files.newInputStreamSupplier(createAppJarFile(cls)));
  }

  protected void addPluginArtifact(Id.Artifact artifactId, Class<?> cls, Manifest manifest,
                                   @Nullable Set<ArtifactRange> parents) throws Exception {
    artifactClient.add(artifactId, parents, Files.newInputStreamSupplier(createArtifactJarFile(cls, manifest)));
  }

  protected void addProperties(Id.Application app, @Nullable Map<String, String> properties) throws Exception {
    metadataClient.addProperties(app, properties);
  }

  protected void addProperties(final Id.Application app, @Nullable final Map<String, String> properties,
                               Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addProperties(app, properties);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addProperties(Id.Artifact artifact, @Nullable Map<String, String> properties) throws Exception {
    metadataClient.addProperties(artifact, properties);
  }

  protected void addProperties(final Id.Artifact artifact, @Nullable final Map<String, String> properties,
                               Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addProperties(artifact, properties);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addProperties(Id.Program program, @Nullable Map<String, String> properties) throws Exception {
    metadataClient.addProperties(program, properties);
  }

  protected void addProperties(final Id.Program program, @Nullable final Map<String, String> properties,
                               Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addProperties(program, properties);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addProperties(Id.DatasetInstance dataset, @Nullable Map<String, String> properties) throws Exception {
    metadataClient.addProperties(dataset, properties);
  }

  protected void addProperties(final Id.DatasetInstance dataset, @Nullable final Map<String, String> properties,
                               Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addProperties(dataset, properties);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addProperties(Id.Stream stream, @Nullable Map<String, String> properties)
    throws Exception {
    metadataClient.addProperties(stream, properties);
  }

  protected void addProperties(Id.Stream.View view, @Nullable Map<String, String> properties)
    throws Exception {
    metadataClient.addProperties(view, properties);
  }

  protected void addProperties(final Id.Stream stream, @Nullable final Map<String, String> properties,
                               Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addProperties(stream, properties);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addProperties(final Id.Stream.View view, @Nullable final Map<String, String> properties,
                               Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addProperties(view, properties);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected Set<MetadataRecord> getMetadata(Id.Application app) throws Exception {
    return getMetadata(app, null);
  }

  protected Set<MetadataRecord> getMetadata(Id.Artifact artifact) throws Exception {
    return getMetadata(artifact, null);
  }

  protected Set<MetadataRecord> getMetadata(Id.Program program) throws Exception {
    return getMetadata(program, null);
  }

  protected Set<MetadataRecord> getMetadata(Id.DatasetInstance dataset) throws Exception {
    return getMetadata(dataset, null);
  }

  protected Set<MetadataRecord> getMetadata(Id.Stream stream) throws Exception {
    return getMetadata(stream, null);
  }

  protected Set<MetadataRecord> getMetadata(Id.Stream.View view) throws Exception {
    return getMetadata(view, null);
  }

  protected Set<MetadataRecord> getMetadata(Id.Application app, @Nullable MetadataScope scope) throws Exception {
    return metadataClient.getMetadata(app, scope);
  }

  protected Set<MetadataRecord> getMetadata(Id.Artifact artifact, @Nullable MetadataScope scope) throws Exception {
    return metadataClient.getMetadata(artifact, scope);
  }

  protected Set<MetadataRecord> getMetadata(Id.Program program, @Nullable MetadataScope scope) throws Exception {
    return metadataClient.getMetadata(program, scope);
  }

  protected Set<MetadataRecord> getMetadata(Id.DatasetInstance dataset,
                                            @Nullable MetadataScope scope) throws Exception {
    return metadataClient.getMetadata(dataset, scope);
  }

  protected Set<MetadataRecord> getMetadata(Id.Stream stream, @Nullable MetadataScope scope) throws Exception {
    return metadataClient.getMetadata(stream, scope);
  }

  protected Set<MetadataRecord> getMetadata(Id.Stream.View view, @Nullable MetadataScope scope) throws Exception {
    return metadataClient.getMetadata(view, scope);
  }

  protected Map<String, String> getProperties(Id.Application app, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(app, scope).iterator()).getProperties();
  }

  protected Map<String, String> getProperties(Id.Artifact artifact, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(artifact, scope).iterator()).getProperties();
  }

  protected Map<String, String> getProperties(Id.Program program, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(program, scope).iterator()).getProperties();
  }

  protected Map<String, String> getProperties(Id.DatasetInstance dataset, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(dataset, scope).iterator()).getProperties();
  }

  protected Map<String, String> getProperties(Id.Stream stream, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(stream, scope).iterator()).getProperties();
  }

  protected Map<String, String> getProperties(Id.Stream.View view, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(view, scope).iterator()).getProperties();
  }

  protected void getPropertiesFromInvalidEntity(Id.Application app) throws Exception {
    try {
      getProperties(app, MetadataScope.USER);
      Assert.fail("Expected not to be able to get properties from invalid entity: " + app);
    } catch (NotFoundException expected) {
      // expected
    }
  }

  protected void getPropertiesFromInvalidEntity(Id.Program program) throws Exception {
    try {
      getProperties(program, MetadataScope.USER);
      Assert.fail("Expected not to be able to get properties from invalid entity: " + program);
    } catch (NotFoundException expected) {
      // expected
    }
  }

  protected void getPropertiesFromInvalidEntity(Id.DatasetInstance dataset) throws Exception {
    try {
      getProperties(dataset, MetadataScope.USER);
      Assert.fail("Expected not to be able to get properties from invalid entity: " + dataset);
    } catch (NotFoundException expected) {
      // expected
    }
  }

  protected void getPropertiesFromInvalidEntity(Id.Stream stream) throws Exception {
    try {
      getProperties(stream, MetadataScope.USER);
      Assert.fail("Expected not to be able to get properties from invalid entity: " + stream);
    } catch (NotFoundException expected) {
      // expected
    }
  }
  protected void getPropertiesFromInvalidEntity(Id.Stream.View view) throws Exception {
    try {
      getProperties(view, MetadataScope.USER);
      Assert.fail("Expected not to be able to get properties from invalid entity: " + view);
    } catch (NotFoundException expected) {
      // expected
    }
  }

  protected void removeMetadata(Id.Application app) throws Exception {
    metadataClient.removeMetadata(app);
  }

  protected void removeMetadata(Id.Artifact artifact) throws Exception {
    metadataClient.removeMetadata(artifact);
  }

  protected void removeMetadata(Id.Program program) throws Exception {
    metadataClient.removeMetadata(program);
  }

  protected void removeMetadata(Id.DatasetInstance dataset) throws Exception {
    metadataClient.removeMetadata(dataset);
  }

  protected void removeMetadata(Id.Stream stream) throws Exception {
    metadataClient.removeMetadata(stream);
  }

  protected void removeMetadata(Id.Stream.View view) throws Exception {
    metadataClient.removeMetadata(view);
  }

  protected void removeProperties(Id.Application app) throws Exception {
    metadataClient.removeProperties(app);
  }

  private void removeProperty(Id.Application app, String propertyToRemove) throws Exception {
    metadataClient.removeProperty(app, propertyToRemove);
  }

  protected void removeProperties(Id.Artifact artifact) throws Exception {
    metadataClient.removeProperties(artifact);
  }

  private void removeProperty(Id.Artifact artifact, String propertyToRemove) throws Exception {
    metadataClient.removeProperty(artifact, propertyToRemove);
  }

  protected void removeProperties(Id.Program program) throws Exception {
    metadataClient.removeProperties(program);
  }

  protected void removeProperty(Id.Program program, String propertyToRemove) throws Exception {
    metadataClient.removeProperty(program, propertyToRemove);
  }

  protected void removeProperties(Id.DatasetInstance dataset) throws Exception {
    metadataClient.removeProperties(dataset);
  }

  protected void removeProperty(Id.DatasetInstance dataset, String propertyToRemove) throws Exception {
    metadataClient.removeProperty(dataset, propertyToRemove);
  }

  protected void removeProperties(Id.Stream stream) throws Exception {
    metadataClient.removeProperties(stream);
  }

  protected void removeProperties(Id.Stream.View view) throws Exception {
    metadataClient.removeProperties(view);
  }

  protected void removeProperty(Id.Stream stream, String propertyToRemove) throws Exception {
    metadataClient.removeProperty(stream, propertyToRemove);
  }

  protected void removeProperty(Id.Stream.View view, String propertyToRemove) throws Exception {
    metadataClient.removeProperty(view, propertyToRemove);
  }

  protected void addTags(Id.Application app, @Nullable Set<String> tags) throws Exception {
    metadataClient.addTags(app, tags);
  }

  protected void addTags(final Id.Application app, @Nullable final Set<String> tags,
                         Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addTags(app, tags);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addTags(Id.Artifact artifact, @Nullable Set<String> tags) throws Exception {
    metadataClient.addTags(artifact, tags);
  }

  protected void addTags(final Id.Artifact artifact, @Nullable final Set<String> tags,
                         Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addTags(artifact, tags);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addTags(Id.Program program, @Nullable Set<String> tags)
    throws Exception {
    metadataClient.addTags(program, tags);
  }

  protected void addTags(final Id.Program program, @Nullable final Set<String> tags,
                         Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addTags(program, tags);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addTags(Id.DatasetInstance dataset, @Nullable Set<String> tags) throws Exception {
    metadataClient.addTags(dataset, tags);
  }

  protected void addTags(final Id.DatasetInstance dataset, @Nullable final Set<String> tags,
                         Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addTags(dataset, tags);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addTags(Id.Stream stream, @Nullable Set<String> tags) throws Exception {
    metadataClient.addTags(stream, tags);
  }

  protected void addTags(Id.Stream.View view, @Nullable Set<String> tags) throws Exception {
    metadataClient.addTags(view, tags);
  }

  protected void addTags(final Id.Stream stream, @Nullable final Set<String> tags,
                         Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addTags(stream, tags);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected void addTags(final Id.Stream.View view, @Nullable final Set<String> tags,
                         Class<? extends Exception> expectedExceptionClass) throws IOException {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        addTags(view, tags);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected Set<MetadataSearchResultRecord> searchMetadata(Id.Namespace namespaceId, String query,
                                                           Set<MetadataSearchTargetType> targets)
    throws Exception {
    return metadataClient.searchMetadata(namespaceId, query, targets);
  }

  protected Set<String> getTags(Id.Application app, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(app, scope).iterator()).getTags();
  }

  protected Set<String> getTags(Id.Artifact artifact, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(artifact, scope).iterator()).getTags();
  }

  protected Set<String> getTags(Id.Program program, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(program, scope).iterator()).getTags();
  }

  protected Set<String> getTags(Id.DatasetInstance dataset, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(dataset, scope).iterator()).getTags();
  }

  protected Set<String> getTags(Id.Stream stream, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(stream, scope).iterator()).getTags();
  }

  protected Set<String> getTags(Id.Stream.View view, MetadataScope scope) throws Exception {
    return Iterators.getOnlyElement(getMetadata(view, scope).iterator()).getTags();
  }

  protected void removeTags(Id.Application app) throws Exception {
    metadataClient.removeTags(app);
  }

  protected void removeTag(Id.Application app, String tagToRemove) throws Exception {
    metadataClient.removeTag(app, tagToRemove);
  }

  protected void removeTags(Id.Artifact artifact) throws Exception {
    metadataClient.removeTags(artifact);
  }

  protected void removeTag(Id.Artifact artifact, String tagToRemove) throws Exception {
    metadataClient.removeTag(artifact, tagToRemove);
  }

  protected void removeTags(Id.Program program) throws Exception {
    metadataClient.removeTags(program);
  }

  private void removeTag(Id.Program program, String tagToRemove) throws Exception {
    metadataClient.removeTag(program, tagToRemove);
  }

  protected void removeTags(Id.DatasetInstance dataset) throws Exception {
    metadataClient.removeTags(dataset);
  }

  protected void removeTag(Id.DatasetInstance dataset, String tagToRemove) throws Exception {
    metadataClient.removeTag(dataset, tagToRemove);
  }

  protected void removeTags(Id.Stream stream) throws Exception {
    metadataClient.removeTags(stream);
  }

  protected void removeTags(Id.Stream.View view) throws Exception {
    metadataClient.removeTags(view);
  }

  protected void removeTag(Id.Stream stream, String tagToRemove) throws Exception {
    metadataClient.removeTag(stream, tagToRemove);
  }

  protected void removeTag(Id.Stream.View view, String tagToRemove) throws Exception {
    metadataClient.removeTag(view, tagToRemove);
  }

  // expect an exception during fetching of lineage
  protected void fetchLineage(Id.DatasetInstance datasetInstance, long start, long end, int levels,
                              Class<? extends Exception> expectedExceptionClass) throws Exception {
    fetchLineage(datasetInstance, Long.toString(start), Long.toString(end), levels, expectedExceptionClass);
  }

  // expect an exception during fetching of lineage
  protected void fetchLineage(final Id.DatasetInstance datasetInstance, final String start, final String end,
                              final int levels, Class<? extends Exception> expectedExceptionClass) throws Exception {
    expectException(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        fetchLineage(datasetInstance, start, end, levels);
        return null;
      }
    }, expectedExceptionClass);
  }

  protected LineageRecord fetchLineage(Id.DatasetInstance datasetInstance, long start, long end,
                                       int levels) throws Exception {
    return lineageClient.getLineage(datasetInstance, start, end, levels);
  }

  protected LineageRecord fetchLineage(Id.DatasetInstance datasetInstance, long start, long end,
                                       Set<CollapseType> collapseTypes, int levels) throws Exception {
    return lineageClient.getLineage(datasetInstance, start, end, collapseTypes, levels);
  }

  protected LineageRecord fetchLineage(Id.DatasetInstance datasetInstance, String start, String end,
                                       int levels) throws Exception {
    return lineageClient.getLineage(datasetInstance, start, end, levels);
  }

  protected LineageRecord fetchLineage(Id.Stream stream, long start, long end, int levels) throws Exception {
    return lineageClient.getLineage(stream, start, end, levels);
  }

  protected LineageRecord fetchLineage(Id.Stream stream, String start, String end, int levels) throws Exception {
    return lineageClient.getLineage(stream, start, end, levels);
  }

  protected LineageRecord fetchLineage(Id.Stream stream, long start, long end, Set<CollapseType> collapseTypes,
                                       int levels) throws Exception {
    return lineageClient.getLineage(stream, start, end, collapseTypes, levels);
  }

  protected Set<MetadataRecord> fetchRunMetadata(Id.Run run) throws Exception {
    return metadataClient.getMetadata(run);
  }

  protected void assertRunMetadataNotFound(Id.Run run) throws Exception {
    try {
      fetchRunMetadata(run);
      Assert.fail("Excepted not to fetch Metadata for a nonexistent Run.");
    } catch (NotFoundException expected) {
      // expected
    }
  }

  private <T> void expectException(Callable<T> callable, Class<? extends Exception> expectedExceptionClass) {
    try {
      callable.call();
      Assert.fail("Expected to have exception of class: " + expectedExceptionClass);
    } catch (Exception e) {
      Assert.assertTrue(e.getClass() == expectedExceptionClass);
    }
  }
}
