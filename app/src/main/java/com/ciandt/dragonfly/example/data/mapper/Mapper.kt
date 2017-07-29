package com.ciandt.dragonfly.example.data.mapper

abstract class Mapper<T> {

    abstract fun map(): T?
}