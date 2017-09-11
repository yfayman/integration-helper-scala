package com.draugrsoft.integraiton.helper.marshallers

import spray.json._
import com.draugrsoft.integration.helper.messages.CommonActorMessages._
import com.draugrsoft.integration.helper.constants.JobAction.JobActionEnum
import com.draugrsoft.integration.helper.constants.JobAction.JobActionEnum

trait IntegrationMarshalling extends DefaultJsonProtocol {

  import com.draugrsoft.integration.helper.constants.MessageLevel._
  import com.draugrsoft.integration.helper.constants.JobStatus._
  import com.draugrsoft.integration.helper.constants.JobAction._

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
  implicit val jobAttributeMarshaller: RootJsonFormat[JobAttribute] = jsonFormat2(JobAttribute)
  implicit val jobParamMarshaller:RootJsonFormat[JobParam] =jsonFormat2(JobParam)
  implicit val jobMessageMarshaller: RootJsonFormat[JobMessage] = jsonFormat2(JobMessage)
 // implicit val jobInstanceShortMarshaller: RootJsonFormat[JobInstanceShort] = jsonFormat4(JobInstanceShort)
  implicit val jobActionMarshaller: RootJsonFormat[JobAction] = jsonFormat2(JobAction)

  // rely on converters above
  implicit val integrationShortMarshaller: RootJsonFormat[IntegrationRecentStatus] = jsonFormat2(IntegrationRecentStatus)
  implicit val jobDataMarshaller: RootJsonFormat[JobInstanceData] = jsonFormat8(JobInstanceData)
 // implicit val jobShortMarshaller: RootJsonFormat[JobShort] = jsonFormat2(JobShort)
  
  // rely on converts above
  implicit val updateJobActionResponseMarshaller: RootJsonFormat[UpdateJobResponse] = jsonFormat1(UpdateJobResponse)

}
