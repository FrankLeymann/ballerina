/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.ballerina.swagger.convertor.service;

import io.swagger.models.Swagger;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.parser.converter.SwaggerConverter;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.ballerina.swagger.convertor.Constants;
import org.ballerinalang.composer.service.ballerina.parser.service.model.BFile;
import org.ballerinalang.composer.service.ballerina.parser.service.util.ParserUtils;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.IOException;

import java.util.stream.Collectors;

/**
 * Swagger related utility classes.
 */

public class SwaggerConverterUtils {
    
    /**
     * This method will generate ballerina string from swagger definition. Since ballerina service definition is super
     * set of swagger definition we will take both swagger and ballerina definition and merge swagger changes to
     * ballerina definition selectively to prevent data loss
     *
     * @param ballerinaSource ballerina definition to be process as ballerina definition
     * @param serviceName service name
     * @return String representation of converted ballerina source
     * @throws IOException when error occur while processing input swagger and ballerina definitions.
     */
    public static String generateSwaggerDefinitions(String ballerinaSource, String serviceName) throws IOException {
        // Get the ballerina model using the ballerina source code.
        BFile balFile = new BFile();
        balFile.setContent(ballerinaSource);
        BLangCompilationUnit topCompilationUnit = ParserUtils.compileFragment(balFile.getContent());
        String httpAlias = getAlias(topCompilationUnit, Constants.BALLERINA_HTTP_PACKAGE_NAME);
        String swaggerAlias = getAlias(topCompilationUnit, Constants.SWAGGER_PACKAGE_NAME);
        SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper(httpAlias, swaggerAlias);
        String swaggerSource = StringUtils.EMPTY;
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangService) {
                ServiceNode serviceDefinition = (ServiceNode) topLevelNode;
                // Generate swagger string for the mentioned service name.
                if (StringUtils.isNotBlank(serviceName)) {
                    if (serviceDefinition.getName().getValue().equals(serviceName)) {
                        Swagger swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger(serviceDefinition);
                        swaggerSource = swaggerServiceMapper.generateSwaggerString(swaggerDefinition);
                        break;
                    }
                } else {
                    // If no service name mentioned, then generate swagger definition for the first service.
                    Swagger swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger(serviceDefinition);
                    swaggerSource = swaggerServiceMapper.generateSwaggerString(swaggerDefinition);
                    break;
                }
            }
        }
    
        return swaggerSource;
    }


    /**
     * This method will generate open API 3.X specification for given ballerina service. Since we will need to
     * support both swagger 2.0 and OAS 3.0 it was implemented to convert to swagger by default and convert it
     * to OAS on demand.
     *
     * @param ballerinaSource ballerina source to be converted to swagger/OAS definition
     * @param serviceName specific service name within ballerina source that need to map OAS
     * @return Generated OAS3 string output.
     * @throws IOException When error occurs while converting, parsing input source.
     */
    public static String generateOAS3Definitions(String ballerinaSource, String serviceName) throws IOException {
        // Get the ballerina model using the ballerina source code.
        BFile balFile = new BFile();
        balFile.setContent(ballerinaSource);
        //Create empty swagger object.
        Swagger swaggerDefinition = new Swagger();
        BLangCompilationUnit topCompilationUnit = ParserUtils.compileFragment(balFile.getContent());;
        String httpAlias = getAlias(topCompilationUnit, Constants.BALLERINA_HTTP_PACKAGE_NAME);
        String swaggerAlias = getAlias(topCompilationUnit, Constants.SWAGGER_PACKAGE_NAME);
        SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper(httpAlias, swaggerAlias);
        String swaggerSource = StringUtils.EMPTY;
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangService) {
                ServiceNode serviceDefinition = (ServiceNode) topLevelNode;
                // Generate swagger string for the mentioned service name.
                if (StringUtils.isNotBlank(serviceName)) {
                    if (serviceDefinition.getName().getValue().equals(serviceName)) {
                        swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger(serviceDefinition);
                        break;
                    }
                } else {
                    // If no service name mentioned, then generate swagger definition for the first service.
                    swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger(serviceDefinition);
                    break;
                }
            }
        }
        swaggerSource = swaggerServiceMapper.generateSwaggerString(swaggerDefinition);
        SwaggerConverter converter = new SwaggerConverter();
        return Yaml.pretty(converter.readContents(swaggerSource, null, null).getOpenAPI());
    }


    /**
     * Gets the alias for a given package from a bLang file root node.
     * @param topCompilationUnit The root node.
     * @param packageName The package name.
     * @return The alias.
     */
    private static String getAlias(BLangCompilationUnit topCompilationUnit, String packageName) {
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangImportPackage) {
                BLangImportPackage importPackage = (BLangImportPackage) topLevelNode;
                String packagePath = importPackage.getPackageName().stream().map(BLangIdentifier::getValue).collect
                        (Collectors.joining("."));
                if (packageName.equals(packagePath)) {
                    return importPackage.getAlias().getValue();
                }
            }
        }
        
        return null;
    }
}
