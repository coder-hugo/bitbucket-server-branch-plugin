package org.jenkinsci.plugins.bitbucket.server;

import net.karneim.pojobuilder.GeneratePojoBuilder;

/**
 * @author Robin Müller
 */
@GeneratePojoBuilder(withFactoryMethod = "a*", withSetterNamePattern = "*", intoPackage = "*.builder")
public @interface BitbucketPojoBuilder {
}
