package okohub.azure.cosmosdb.junit;

/**
 * @author Onur Kagan Ozcan
 */
interface ResourceOperator {

  void createDatabase();

  void createContainer();

  void populate() throws Exception;

  void deleteDatabase();
}
