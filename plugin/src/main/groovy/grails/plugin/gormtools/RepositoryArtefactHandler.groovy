/*
 * Copyright (c) 2011 Joshua Burnett or other authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.gormtools

import grails.core.ArtefactHandlerAdapter
import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic

import java.util.regex.Pattern

import static org.grails.io.support.GrailsResourceUtils.GRAILS_APP_DIR
import static org.grails.io.support.GrailsResourceUtils.REGEX_FILE_SEPARATOR

/**
 * Grails artefact handler for repository classes
 *
 * @author Joshua Burnett
 */
@CompileStatic
class RepositoryArtefactHandler extends ArtefactHandlerAdapter {

    static final String TYPE = "Repository"
    static final String SUFFIX = "Repo"
    static final String PLUGIN_NAME = "gorm-tools"
    static final Pattern REPO_PATH_PATTERN = Pattern.compile(".+" + REGEX_FILE_SEPARATOR + GRAILS_APP_DIR +
        REGEX_FILE_SEPARATOR + "repository" + REGEX_FILE_SEPARATOR + "(.+)\\.(groovy)")

    RepositoryArtefactHandler() {
        super(TYPE, GrailsRepositoryClass.class, DefaultGrailsRepositoryClass.class, SUFFIX, false)
    }

//    boolean isArtefact(ClassNode classNode) {
//        if(classNode == null ||
//            !isValidArtefactClassNode(classNode, classNode.getModifiers()) ||
//            !classNode.getName().endsWith(SUFFIX) ) {
//            return false
//        }
//
//        URL url = GrailsASTUtils.getSourceUrl(classNode)
//
//        url &&  REPO_PATH_PATTERN.matcher(url.getFile()).find()
//    }

    boolean isArtefactClass(Class clazz) {
        // class shouldn't be null and should ends with Job suffix
        (clazz != null && clazz.getName().endsWith(SUFFIX))
    }

    @Override
    String getPluginName() {
        PLUGIN_NAME
    }

    /** Static helpers for other classes that need to look up the spring beans */
    static String getRepoClassName(Class domainClass) {
        return "${domainClass.name}$SUFFIX"
    }

    static String getRepoBeanName(Class domainClass) {
        return "${GrailsNameUtils.getPropertyName(domainClass.name)}$SUFFIX"
    }
}