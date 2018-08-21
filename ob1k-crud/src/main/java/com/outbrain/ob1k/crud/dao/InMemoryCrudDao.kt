package com.outbrain.ob1k.crud.dao

import com.google.gson.JsonObject
import com.outbrain.ob1k.concurrent.ComposableFuture
import com.outbrain.ob1k.concurrent.ComposableFutures
import com.outbrain.ob1k.crud.model.Entities

class InMemoryCrudDao(private var idFieldName: String = "id", var resourceName: String) : ICrudAsyncDao<JsonObject> {
    private val map: MutableMap<Int, JsonObject> = mutableMapOf()

    override fun list(pagination: IntRange, sort: Pair<String, String>, filter: JsonObject): ComposableFuture<Entities<JsonObject>> {
        val list = map.values.filter { it.match(filter) }.drop(pagination.first).take(pagination.last + 1 - pagination.first)
        val sortedList = if (sort.second == "ASC") list.sortedBy { it.get(sort.first).asString } else list.sortedByDescending { it.get(sort.first).asString }
        return ComposableFutures.fromValue(Entities(map.size, sortedList))
    }

    override fun read(id: Int): ComposableFuture<JsonObject?> {
        return ComposableFutures.fromValue(map.get(id))
    }

    override fun create(entity: JsonObject): ComposableFuture<JsonObject> {
        val id = map.size + 1
        entity.addProperty(idFieldName, id)
        map.put(id, entity)
        return ComposableFutures.fromValue(entity)
    }

    override fun update(id: Int, entity: JsonObject): ComposableFuture<JsonObject> {
        map.put(id, entity)
        return ComposableFutures.fromValue(entity)
    }

    override fun delete(id: Int): ComposableFuture<Int> {
        map.remove(id)
        return ComposableFutures.fromValue(id)
    }

    override fun resourceName() = resourceName

    private fun JsonObject.match(other: JsonObject): Boolean {
        val entrySet = other.entrySet()
        if (entrySet.isEmpty()) {
            return true
        }
        return entrySet.map { it.key }.all {
            val myElement = this.get(it).asJsonPrimitive
            val otherElement = other.get(it).asJsonPrimitive
            when {
                myElement == null -> false
                myElement.asJsonPrimitive.isNumber -> myElement.asNumber == otherElement.asNumber
                myElement.asJsonPrimitive.isBoolean -> myElement.asBoolean == otherElement.asBoolean
                myElement.asJsonPrimitive.isString -> myElement.asString.contains(otherElement.asString)
                else -> false
            }
        }
    }
}