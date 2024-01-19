package com.github.senocak.domain.dto

interface IResource {
    fun validate(): Exception?
}