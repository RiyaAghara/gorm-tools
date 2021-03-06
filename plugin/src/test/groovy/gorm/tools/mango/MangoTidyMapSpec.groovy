/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.mango

import spock.lang.Specification

class MangoTidyMapSpec extends Specification {

    void "test pathToMap"() {
        expect:
        [a: [b: [c: 1]]] == MangoTidyMap.pathToMap("a.b.c", 1, [:])
        flatten([a: [b: [c: 1]], d: 2]) == flatten(MangoTidyMap.pathToMap("a.b.c", 1, [d: 2]))
        flatten([a: 1, d: 2]) == flatten(MangoTidyMap.pathToMap("a", 1, [d: 2]))
    }

    void "test tidy method for equal"() {
        expect:
        [a: [b: [c: ['$eq': 1]]]] == MangoTidyMap.tidy(["a.b.c": 1])
        [a: [b: [c: ['$eq': 1]]], d: ['$eq': 2]] == MangoTidyMap.tidy(["a.b.c": 1, d: ['$eq': 2]])
        [a: ['$eq': 1], d: ['$eq': 2]] == MangoTidyMap.tidy(["a": 1, d: 2])
        [a: [b: [c: ['$eq': 1]]]] == MangoTidyMap.tidy([a: [b: [c: 1]]])
    }

    void "test in"() {
        when:
        def mmap = tidy([
            'foo.id'     : [1, 2, 3],
            'customer.id': ['$in': [1, 2, 3]]
        ])

        then:
        mmap == [
            foo     : [
                id: [
                    '$in': [1, 2, 3]
                ]
            ],
            customer: [
                id: [
                    '$in': [1, 2, 3]
                ]
            ]
        ]

        when:
        mmap = tidy([
            "customer": [["id": 1], ["id": 2], ["id": 3]]
        ])

        then:
        flatten(mmap) == flatten([
            customer: [
                id: [
                    '$in': [1, 2, 3]
                ]
            ]
        ])
    }

    void "test like"() {
        when:
        def mmap = tidy([
            'foo.name': "Name%"
        ])

        then:
        mmap == [foo: [name: ['$ilike': "Name%"]]]

        when:
        mmap = tidy(
            [foo:
                 [name: "Name%"]
            ])

        then:
        mmap == [foo: [name: ['$ilike': "Name%"]]]
    }

    void "test eq"() {
        when:
        def mmap = tidy([
            'foo.name': "Name"
        ])

        then:

        mmap == [foo: [name: ['$eq': "Name"]]]

        when:
        mmap = tidy([
            foo: [name: "Name"]
        ])

        then:

        mmap == [foo: [name: ['$eq': "Name"]]]
    }

    void "test combined methods"() {
        when:
        def mmap = tidy([
            "customer": [
                "id"  : 101,
                "name": "Wal%"
            ]
        ])

        then:

        flatten(mmap) == flatten([customer: [id: ['$eq': 101], name: ['$ilike': 'Wal%']]])

    }

    void "test \$or"() {
        when:
        def mmap = tidy([
            '$or': [
                "id"  : 101,
                "name": "Wal%"
            ]
        ])

        then:

        mmap == ['$or': [[id: ['$eq': 101]], [name: ['$ilike': 'Wal%']]]]

    }

    void "test \$or with and"() {
        when:
        def mmap = tidy(['$or': [["address.id": 5], ["name": "Org#1", "address.id": 4]]])

        then:

        flatten(mmap) == flatten([
            '$or': [
                [
                    '$and': [[address: [
                        id: ['$eq': 5]
                    ]]]],
                ['$and': [
                    [name: ['$eq': "Org#1"]],
                    [address: [
                        id: ['$eq': 4]
                    ]]
                ]
                ]]
        ]
        )
    }


    void "test if map has Mango method "() {
        when:
        def mmap = tidy([
            'foo.name.$eq': "Name"
        ])

        then:
        mmap == [foo: [name: ['$eq': "Name"]]]

        when:
        mmap = tidy([
            'foo.name.$like': "Name"
        ])

        then:
        mmap == [foo: [name: ['$like': "Name"]]]

        when:
        mmap = tidy([
            foo: ['name.$like': "Name"]
        ])

        then:
        mmap == [foo: [name: ['$like': "Name"]]]
    }

    Map tidy(Map m) {
        MangoTidyMap.tidy(m)
    }

    Map flatten(Map m, String separator = '.') {
        m.collectEntries { k, v -> v instanceof Map ? flatten(v, separator).collectEntries { q, r -> [(k + separator + q): r] } : [(k): v] }
    }

}
