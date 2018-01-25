package com.draugrsoft.integration.helper.connector.test

import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages.Integration
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.util.concurrent.TimeUnit
import org.scalatest.WordSpec
import org.scalatest.Matchers
import com.draugrsoft.integration.helper.messages.IntegrationModuleMessages.JobWithProps
import com.draugrsoft.integraiton.helper.DummyJobActor
import scala.util.{Success,Failure}
import com.draugrsoft.integration.helper.messages.CommonActorMessages.JobAction
import com.draugrsoft.integration.helper.connector.IntegrationHelperConnector;
import com.draugrsoft.integration.helper.constants.JobAction.JobActionEnum
import com.draugrsoft.integration.helper.constants.JobAction.JobActionEnum
import com.draugrsoft.integration.helper.constants.JobAction.StartAction

class IntegrationHelperConnectorSpec extends WordSpec with Matchers {
  
  class IntegrationHelperConnectorTest extends IntegrationHelperConnector{
    
    lazy val testIntegrations =  Integration("integration1", JobWithProps("jerbOne", DummyJobActor.props) :: JobWithProps("jerbTwo", DummyJobActor.props) :: Nil)
    
    def integrations: List[Integration] = List(testIntegrations)
  
    implicit val timeout: Timeout =  Timeout(50, TimeUnit.SECONDS)    
    implicit val system: ActorSystem = ActorSystem("http-system")
    implicit val ec: ExecutionContext = system.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()
  }
  
 
  
  "Connector" should {
     val connector = new IntegrationHelperConnectorTest
     implicit val ec = ExecutionContext.global
     
     "get job info that exists" in {
       connector.getJobInfo("integration1", "jerbOne").onComplete({
         case Success(jidOpt) => {
           jidOpt.fold(fail("Received no response back when one was expected"))
                      { _ =>  succeed }
         }
         case Failure(e) => fail("Got an unexpected error")
       })
     }
     
     "get an empty response if the job doesn't exist" in {
       connector.getJobInfo("integration5", "jerbOne").onComplete({
         case Success(jidOpt) => {
           jidOpt.fold(succeed)
                      { _ =>  fail("Somehow got a response back when integration/job didn't exist") }
         }
         case Failure(e) => fail("Got an unexpected error")
       })
     }
     
     "Get jobs info should respond if the integration exists" in {
       connector.getJobsInfo("integration1").onComplete({
         case Success(integrationStatusOpt) => 
           integrationStatusOpt.fold(fail("Did not receive any data"))
                                    {_ => succeed}
         case Failure(e) => fail("Got an unexpected error")
       })
     }
     
     "Get jobs info should respond if the integration that doesn't exist" in {
       connector.getJobsInfo("integration44").onComplete({
         case Success(integrationStatusOpt) => 
           integrationStatusOpt.fold(succeed)
                                    {_ => fail("Got data for integration that doesn't exist")}
         case Failure(e) => fail("Got an unexpected error")
       })
     }
     
     "Stop Jobs should successfully stop all jobs for an integration that exists" in {
       connector.stopJobs("integration1").onComplete({
         case Success(updateJobResponseXs) => succeed
         case Failure(e) => fail("Got an unexpected error")
       })
     }
     
     "Stop Jobs should stop no jobs for an integration that doesn't exists" in {
       connector.stopJobs("integration9").onComplete({
         case Success(updateJobResponseXs) => assertResult(0)(updateJobResponseXs.size)
         case Failure(e) => fail("Got an unexpected error")
       })
     }
     
     "Get integrations info should return something " in {
       connector.getIntegrationsInfo.onComplete({
         case Success(intData) => assert(!intData.isEmpty)
         case Failure(e) => fail("Got an unexpected error")
       })
     }
     
     "IntegrationAction should return a response for an integration that exists" in {
       connector.integrationAction("integration1", JobAction(StartAction,Nil)).onComplete({
         case Success(updateJobResponseXs) => assert(!updateJobResponseXs.isEmpty)
         case Failure(e) =>  fail("Got an unexpected error")
       })
     }
     
      "IntegrationAction should not return a response for an integration that exists" in {
       connector.integrationAction("integration1", JobAction(StartAction,Nil)).onComplete({
         case Success(updateJobResponseXs) => assertResult(2)(updateJobResponseXs.size)
         case Failure(e) =>  fail("Got an unexpected error")
       })
     }
     
      "Stop job on an integration that doesn't exist should return nothing" in {
        connector.stopJob("fakeint", "jerb1").onComplete({
          case Success(jidOpt) => assert(jidOpt.isEmpty)
          case Failure(e) => fail("Got an unexpected error")
        })
      }
     
      "Stop job on an integration that exists, but job doesn't exist should return nothing" in {
        connector.stopJob("integration1","jerbNinty").onComplete({
           case Success(jidOpt) => assert(jidOpt.isEmpty)
           case Failure(e) => fail(s"Got an unexpected error $e")
        })
      }
      
      "Stop job on an integration that exists and a job that exists should return data" in {
        connector.stopJob("integration1","jerbOne").onComplete({
           case Success(jidOpt) => assert(jidOpt.isDefined)
           case Failure(e) => fail(s"Got an unexpected error $e")
        })
      }
     
  }
  
  
}