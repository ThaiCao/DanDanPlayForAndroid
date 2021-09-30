package com.xyoye.stream_component.utils.manager

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/9/30
 *     desc  :
 * </pre>
 */
class StreamException : Exception {
    constructor(
        type: StreamType,
        message: String?
    ) : super("${type.name} $message")

    constructor(
        type: StreamType,
        message: String?,
        exception: Exception
    ) : super("${type.name} $message", exception)
}