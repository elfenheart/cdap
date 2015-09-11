/*
 * Copyright © 2014-2015 Cask Data, Inc.
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

package co.cask.cdap.data2.dataset2;

import co.cask.cdap.api.dataset.Dataset;
import co.cask.cdap.api.dataset.DatasetAdmin;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.api.dataset.module.DatasetModule;
import co.cask.cdap.common.ServiceUnavailableException;
import co.cask.cdap.data2.datafabric.dataset.type.DatasetClassLoaderProvider;
import co.cask.cdap.proto.DatasetSpecificationSummary;
import co.cask.cdap.proto.Id;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides access to the Datasets System.
 *
 * Typical usage example:
 * <tt>
 *   DatasetFramework datasetFramework = ...;
 *   datasetFramework.addModule("myDatasets", MyDatasetModule.class);
 *   datasetFramework.addInstance("myTable", "table", DatasetProperties.EMPTY);
 *   TableAdmin admin = datasetFramework.getAdmin("myTable");
 *   admin.create();
 *   Table table = datasetFramework.getDataset("myTable");
 *   try {
 *     table.write("key", "value");
 *   } finally {
 *     table.close();
 *   }
 * </tt>
 */
// todo: use dataset instead of dataset instance in namings
public interface DatasetFramework {

  /**
   * Adds dataset types by adding dataset module to the system.
   *
   * @param moduleId dataset module id
   * @param module dataset module
   * @throws ModuleConflictException when module with same name is already registered or this module registers a type
   *         with a same name as one of the already registered by another module types
   * @throws DatasetManagementException in case of problems
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  void addModule(Id.DatasetModule moduleId, DatasetModule module) throws DatasetManagementException,
    ServiceUnavailableException;

  /**
   * Deletes dataset module and its types from the system.
   *
   * @param moduleId dataset module id
   * @throws ModuleConflictException when module cannot be deleted because of its dependant modules or instances
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  void deleteModule(Id.DatasetModule moduleId) throws DatasetManagementException, ServiceUnavailableException;

  /**
   * Deletes dataset modules and its types in the specified namespace.
   *
   * @param namespaceId the {@link Id.Namespace} to delete all modules from.
   * @throws ModuleConflictException when some of modules can't be deleted because of its dependant modules or instances
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  void deleteAllModules(Id.Namespace namespaceId) throws DatasetManagementException, ServiceUnavailableException;

  /**
   * Adds information about dataset instance to the system.
   *
   * This uses
   * {@link co.cask.cdap.api.dataset.DatasetDefinition#configure(String, DatasetProperties)}
   * method to build {@link co.cask.cdap.api.dataset.DatasetSpecification} which describes dataset instance
   * and later used to initialize {@link DatasetAdmin} and {@link Dataset} for the dataset instance.
   *
   * @param datasetTypeName dataset instance type name
   * @param datasetInstanceId dataset instance name
   * @param props dataset instance properties
   * @throws InstanceConflictException if dataset instance with this name already exists
   * @throws IOException when creation of dataset instance using its admin fails
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  void addInstance(String datasetTypeName, Id.DatasetInstance datasetInstanceId, DatasetProperties props)
    throws DatasetManagementException, IOException, ServiceUnavailableException;

  /**
   * Updates the existing dataset instance in the system.
   *
   * This uses
   * {@link co.cask.cdap.api.dataset.DatasetDefinition#configure(String, DatasetProperties)}
   * method to build {@link co.cask.cdap.api.dataset.DatasetSpecification} with new properties,
   * which describes dataset instance and {@link DatasetAdmin} is used to upgrade
   * {@link Dataset} for the dataset instance.
   * @param datasetInstanceId dataset instance name
   * @param props dataset instance properties
   * @throws IOException when creation of dataset instance using its admin fails
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  void updateInstance(Id.DatasetInstance datasetInstanceId, DatasetProperties props)
    throws DatasetManagementException, IOException, ServiceUnavailableException;

  /**
   * Get all dataset instances in the specified namespace
   *
   * @param namespaceId the specified namespace id
   * @return a collection of {@link DatasetSpecification}s for all datasets in the specified namespace
   */
  Collection<DatasetSpecificationSummary> getInstances(Id.Namespace namespaceId) throws DatasetManagementException,
    ServiceUnavailableException;

  /**
   * Gets the {@link DatasetSpecification} for the specified dataset instance id
   *
   * @param datasetInstanceId the {@link Id.DatasetInstance} for which the {@link DatasetSpecification} is desired
   * @return {@link DatasetSpecification} of the dataset or {@code null} if dataset not not exist
   */
  @Nullable
  DatasetSpecification getDatasetSpec(Id.DatasetInstance datasetInstanceId) throws DatasetManagementException,
    ServiceUnavailableException;

  /**
   * @param datasetInstanceId the {@link Id.DatasetInstance} to check for existence
   * @return true if instance exists, false otherwise
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  boolean hasInstance(Id.DatasetInstance datasetInstanceId) throws DatasetManagementException,
    ServiceUnavailableException;

  /**
   * Checks if the specified type exists in the 'system' namespace
   *
   * @return true if type exists in the 'system' namespace, false otherwise
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  boolean hasSystemType(String typeName) throws DatasetManagementException, ServiceUnavailableException;

  /**
   * Checks if the specified type exists in the specified namespace
   *
   * @return true if type exists in the specified namespace, false otherwise
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  @VisibleForTesting
  boolean hasType(Id.DatasetType datasetTypeId) throws DatasetManagementException, ServiceUnavailableException;

  /**
   * Deletes dataset instance from the system.
   *
   * @param datasetInstanceId dataset instance name
   * @throws InstanceConflictException if dataset instance cannot be deleted because of its dependencies
   * @throws IOException when deletion of dataset instance using its admin fails
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  void deleteInstance(Id.DatasetInstance datasetInstanceId) throws DatasetManagementException, IOException,
    ServiceUnavailableException;

  /**
   * Deletes all dataset instances in the specified namespace.
   *
   * @param namespaceId the specified namespace id
   * @throws IOException when deletion of dataset instance using its admin fails
   * @throws DatasetManagementException
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  void deleteAllInstances(Id.Namespace namespaceId) throws DatasetManagementException, IOException,
    ServiceUnavailableException;

  /**
   * Gets dataset instance admin to be used to perform administrative operations. The given classloader must
   * be able to load all classes needed to instantiate the dataset admin. This means if the system classloader is
   * used, only system dataset admins can fetched.
   *
   * @param <T> dataset admin type
   * @param datasetInstanceId dataset instance name
   * @param classLoader classLoader to be used to load classes or {@code null} to use system classLoader
   * @return instance of dataset admin or {@code null} if dataset instance of this name doesn't exist.
   * @throws DatasetManagementException when there's trouble getting dataset meta info
   * @throws IOException when there's trouble to instantiate {@link DatasetAdmin}
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  @Nullable
  <T extends DatasetAdmin> T getAdmin(Id.DatasetInstance datasetInstanceId, @Nullable ClassLoader classLoader)
    throws DatasetManagementException, IOException, ServiceUnavailableException;

  /**
   * Gets dataset instance admin to be used to perform administrative operations. The class loader provider
   * is used get classloaders for any dataset modules used by the specified dataset admin. This is because
   * the classloader(s) for a dataset admin may create some resources that need to be cleaned up on close.
   *
   * @param <T> dataset admin type
   * @param datasetInstanceId dataset instance name
   * @param classLoader parent classLoader to be used to load classes or {@code null} to use system classLoader
   * @param classLoaderProvider provider to get classloaders for different dataset modules
   * @return instance of dataset admin or {@code null} if dataset instance of this name doesn't exist.
   * @throws DatasetManagementException when there's trouble getting dataset meta info
   * @throws IOException when there's trouble to instantiate {@link DatasetAdmin}
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  @Nullable
  <T extends DatasetAdmin> T getAdmin(Id.DatasetInstance datasetInstanceId,
                                      @Nullable ClassLoader classLoader,
                                      DatasetClassLoaderProvider classLoaderProvider)
    throws DatasetManagementException, IOException, ServiceUnavailableException;

  /**
   * Gets dataset to be used to perform data operations.
   *
   * @param <T> dataset type to be returned
   * @param datasetInstanceId dataset instance id
   * @param arguments runtime arguments for the dataset instance
   * @param classLoader classLoader to be used to load classes or {@code null} to use system classLoader
   * @param owners owners of the dataset
   * @return instance of dataset or {@code null} if dataset instance of this name doesn't exist.
   * @throws DatasetManagementException when there's trouble getting dataset meta info
   * @throws IOException when there's trouble to instantiate {@link co.cask.cdap.api.dataset.Dataset}
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  @Nullable
  <T extends Dataset> T getDataset(Id.DatasetInstance datasetInstanceId, @Nullable Map<String, String> arguments,
                                   @Nullable ClassLoader classLoader, @Nullable Iterable<? extends Id> owners)
    throws DatasetManagementException, IOException, ServiceUnavailableException;

  /**
   * Gets dataset to be used to perform data operations.
   *
   * @param <T> dataset type to be returned
   * @param datasetInstanceId dataset instance id
   * @param arguments runtime arguments for the dataset instance
   * @param classLoader classLoader to be used to load classes or {@code null} to use system classLoader
   * @return instance of dataset or {@code null} if dataset instance of this name doesn't exist.
   * @throws DatasetManagementException when there's trouble getting dataset meta info
   * @throws IOException when there's trouble to instantiate {@link co.cask.cdap.api.dataset.Dataset}
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  @Nullable
  <T extends Dataset> T getDataset(Id.DatasetInstance datasetInstanceId, @Nullable Map<String, String> arguments,
                                   @Nullable ClassLoader classLoader)
    throws DatasetManagementException, IOException, ServiceUnavailableException;

  /**
   * Gets dataset to be used to perform data operations. This one is used when the classloader(s) for a dataset may
   * create some resources that need to be cleaned up on close.
   *
   * @param <T> dataset type to be returned
   * @param datasetInstanceId dataset instance id
   * @param arguments runtime arguments for the dataset instance
   * @param classLoader parent classLoader to be used to load classes or {@code null} to use system classLoader
   * @param classLoaderProvider provider to get classloaders for different dataset modules
   * @param owners owners of the dataset
   * @return instance of dataset or {@code null} if dataset instance of this name doesn't exist.
   * @throws DatasetManagementException when there's trouble getting dataset meta info
   * @throws IOException when there's trouble to instantiate {@link co.cask.cdap.api.dataset.Dataset}
   * @throws ServiceUnavailableException when the dataset service is not running
   */
  @Nullable
  <T extends Dataset> T getDataset(Id.DatasetInstance datasetInstanceId, @Nullable Map<String, String> arguments,
                                   @Nullable ClassLoader classLoader,
                                   DatasetClassLoaderProvider classLoaderProvider,
                                   @Nullable Iterable<? extends Id> owners)
    throws DatasetManagementException, IOException, ServiceUnavailableException;

  /**
   * Creates a namespace in the Storage Providers - HBase/LevelDB, Hive and HDFS/Local File System.
   *
   * @param namespaceId the {@link Id.Namespace} to create
   */
  void createNamespace(Id.Namespace namespaceId) throws DatasetManagementException, ServiceUnavailableException;

  /**
   * Deletes a namespace in the Storage Providers - HBase/LevelDB, Hive and HDFS/Local File System.
   *
   * @param namespaceId the {@link Id.Namespace} to create
   */
  void deleteNamespace(Id.Namespace namespaceId) throws DatasetManagementException, ServiceUnavailableException;
}