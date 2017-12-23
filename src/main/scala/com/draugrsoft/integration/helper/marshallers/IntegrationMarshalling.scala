package com.draugrsoft.integration.helper.marshallers

import spray.json._
import com.draugrsoft.integration.helper.constants.MessageLevel._
import com.draugrsoft.integration.helper.constants.JobStatus._
import com.draugrsoft.integration.helper.constants.JobAction._
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.draugrsoft.integration.helper.actors.MasterJobActor._

trait IntegrationMarshalling extends DefaultJsonProtocol {

  implicit object MessageLevelEnumFormat extends RootJsonFormat[MessageLevelEnum] {
    def write(ml: MessageLevelEnum): JsString = JsString(ml)

    def read(value: JsValue): MessageLevelEnum = value match {
      case JsString(s) => convertStringToMessageLevel(s)
      case _           => deserializationError("expected string")
    }
  }

  implicit object StatusEnumFormat extends RootJsonFormat[JobStatusEnum] {
    def write(s: JobStatusEnum): JsString = JsString(s)

    def read(value: JsValue): JobStatusEnum = value match {
      case JsString(s) => convertStringToJobStatus(s)
      case _           => deserializationError("expected string")
    }
  }

  implicit object JobActionEnumFormat extends RootJsonFormat[JobActionEnum] {
    def write(a: JobActionEnum): JsString = JsString(a)

    def read(value: JsValue): JobActionEnum = value match {
      case JsString(s) => convertStringToJobActionEnum(s)
      case _           => deserializationError("Expected string")
    }
  }

  /**
   * Order is important here. If a marshaller, depends on other marshallers, it must come after.
   * Otherwise there will be a cryptic NPE
   */

  // Simplest case classes
  implicit val jobRecentShortMarshaller: RootJsonFormat[JobRecentStatus] = jsonFormat2(JobRecentStatus)
  implicit val jobParamMarshaller: RootJsonFormat[JobParam] = jsonFormat2(JobParam)
  implicit val jobMessageMarshaller: RootJsonFormat[JobMessage] = jsonFormat2(JobMessage)

  //case class JobAction(action: JobActionEnum, params: List[JobParam])
  implicit object JobActionFormat extends RootJsonFormat[JobAction] {
    def write(ja: JobAction): JsObject = {
      val jsParams = ja.params.map { p => JsObject(
                                            "name" -> JsString(p.name), 
                                             "value" -> JsString(p.value)
                                          )}
      JsObject(
        "action" -> JsString(ja.action),
        "params" -> JsArray(jsParams))
    }

    def read(js: JsValue): JobAction = js.asJsObject.getFields("action", "params") match {
      case Seq(JsString(action), JsArray(params)) => {
        val fixedParams = params.map { jsv =>
          jsv.asJsObject.getFields("name", "value") match {
            case Seq(JsString(name), JsString(value)) => JobParam(name, value)
          }
        }

        JobAction(action, fixedParams.toList)
      }
      case Seq(JsString(action)) => JobAction(action, Nil)
      case _                     => throw new DeserializationException("name/value expected")
    }

  }

  // rely on converters above
  implicit val integrationShortMarshaller: RootJsonFormat[IntegrationRecentStatus] = jsonFormat2(IntegrationRecentStatus)
  implicit val jobDataMarshaller: RootJsonFormat[JobInstanceData] = jsonFormat8(JobInstanceData)

  // rely on converts above
  implicit val updateJobActionResponseMarshaller: RootJsonFormat[UpdateJobResponse] = jsonFormat1(UpdateJobResponse)

}
