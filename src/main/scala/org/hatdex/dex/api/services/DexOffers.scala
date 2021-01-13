/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package org.hatdex.dex.api.services

import java.util.UUID

import io.dataswift.models.dex.OfferClaimsInfo
import play.api.Logger
import play.api.http.Status._
import play.api.libs.ws._

import scala.concurrent.{Future, ExecutionContext}

trait DexOffers {
  val logger: Logger
  val ws: WSClient
  val schema: String
  val dexAddress: String

  import io.dataswift.models.dex.play.DexJsonFormats._

  @deprecated("This endpoint does not exist in dex", since = "0.0.0") //\todo remove this ? what does this affect ? is anyone using it ?
  def offerClaims(
      access_token: String,
      offerId: UUID
    )(implicit ec: ExecutionContext): Future[OfferClaimsInfo] = {
    logger.debug(s"Get Data Debit $offerId values from $dexAddress")

    val request: WSRequest = ws
      .url(s"$schema$dexAddress/api/offer/$offerId/claims")
      .withVirtualHost(dexAddress)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.get()
    futureResponse.map { response =>
      response.status match {
        case OK =>
          val jsResponse = response.json.validate[OfferClaimsInfo] recover {
                case e =>
                  logger.error(s"Error parsing successful offer claims info response: ${e}")
                  throw new RuntimeException(s"Error parsing successful offer claims info response: ${e}")
              }
          // Convert to OfferClaimsInfo - if validation has failed, it will have thrown an error already
          jsResponse.get
        case _ =>
          logger.error(s"Fetching Offer $offerId claims from $dexAddress failed, $response, ${response.body}")
          throw new RuntimeException(
            s"Fetching Offer $offerId claims from $dexAddress failed, $response, ${response.body}"
          )
      }
    }
  }

}
