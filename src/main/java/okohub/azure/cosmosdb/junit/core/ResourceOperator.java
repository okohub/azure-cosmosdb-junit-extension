package okohub.azure.cosmosdb.junit.core;

/**
 * @author Onur Kagan Ozcan
 */
public interface ResourceOperator {

  void createDatabase();

  void createContainer();

  void populate() throws Exception;

  void deleteDatabase();
}
