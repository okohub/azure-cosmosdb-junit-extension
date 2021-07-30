package okohub.azure.cosmosdb.junit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * @author Onur Kagan Ozcan
 */
public interface CosmosDataExtension extends ExecutionCondition,
                                             BeforeEachCallback,
                                             AfterEachCallback,
                                             ParameterResolver {

}
