/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package grails.plugin.gormtools

import gorm.tools.repository.RepoMessage
import grails.util.GrailsNameUtils
import grails.validation.ValidationException
import groovy.transform.CompileDynamic
import org.hibernate.exception.ConstraintViolationException
import org.springframework.context.MessageSource

import java.sql.BatchUpdateException

@CompileDynamic
class ErrorMessageService {

    MessageSource messageSource

    /**
     * Builds the error response from error to make it more human readable.
     * Used in BaseDomain controller and ArTranMassUpdateService, to show all errors that occurred during processing
     *
     * @param e exception object
     * @return map with next fields
     *      code - HTTP response code
     *      message - text message of the error
     *      messageCode - code of the error
     *      errors - list of errors for each entity field
     */
    Map buildErrorResponse(e) {
        int code = 500
        if (e instanceof ValidationException || e instanceof ConstraintViolationException
            || e instanceof org.grails.datastore.mapping.validation.ValidationException) {
            code = 422
        }

        List<Throwable> causes = []
        Throwable curr = e
        while (curr?.cause != null) {
            causes << curr.cause
            curr = curr.cause
        }

        Map errMap = [
            "code"       : code,
            "status"     : "error",
            "message"    : e.hasProperty('messageMap') ? buildMsg(e.messageMap) : e.message,
            "messageCode": e.hasProperty('messageMap') ? e.messageMap.code : 0,
            "errors"     : [:]
        ]

        if (e.hasProperty('errors')) {
            if (e.hasProperty("entity") && e.entity?.errors) {
                errMap.errors = e.entity.errors.fieldErrors.groupBy {
                    GrailsNameUtils.getPropertyNameRepresentation(it.objectName)
                }.each {
                    it.value = it.value.collectEntries {
                        [(it.field): messageSource.getMessage(it, Locale.ENGLISH)]
                    }
                }
            } else if (!e.hasProperty("entity")) {
                errMap.errors = e.errors.fieldErrors.groupBy {
                    GrailsNameUtils.getPropertyNameRepresentation(it.objectName)
                }.each {
                    it.value = it.value.collectEntries {
                        [(it.field): messageSource.getMessage(it, Locale.ENGLISH)]
                    }
                }
            }
        }

        if (e.hasProperty('entity')) {
            BatchUpdateException core = causes.find { it instanceof BatchUpdateException }
            String num = e.entity.hasProperty('num') ? e.entity.num : null
            if (core && core.message.startsWith('Duplicate entry') && num && core.message.contains(num)) {
                errMap.errors = errMap.errors ?: [:]
                errMap.errors.num = 'Duplicate entry'
            }
        }
        return errMap
    }

    String buildMsg(msgMap) {
        Object[] args = (msgMap.args instanceof List) ? msgMap.args as Object[] : [] as Object[]

        return messageSource.getMessage(msgMap.code, args, msgMap.defaultMessage, RepoMessage.defaultLocale())
    }
}
