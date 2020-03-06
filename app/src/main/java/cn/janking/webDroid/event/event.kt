package cn.janking.webDroid.event

/**
 * @author Janking
 */

/**
 * 用于传递打包过程结束的信息
 */
class BuildFinishEvent

/**
 * 用于结束打包任务的信息
 */
class CancelBuildEvent

/**
 * 用于传达初始化完成的信息
 */
class InitFinishEvent(val success: Boolean)