package io.github.okohub.azure.cosmosdb.junit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

/**
 * @author Onur Kagan Ozcan
 */
public interface CosmosDataExtension extends BeforeEachCallback,
                                             AfterEachCallback,
                                             TestInstancePreDestroyCallback,
                                             ParameterResolver {

}
