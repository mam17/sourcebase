package com.example.myapplication.base.adapter.interfaces

interface DiffComparable<T> {
    fun areItemsTheSame(other: T): Boolean
    fun areContentsTheSame(other: T): Boolean
}