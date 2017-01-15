package com.soheila.um

import com.mohiva.play.silhouette.api.services.AvatarService

import scala.concurrent.Future

class TestGravatarService extends AvatarService {

  override def retrieveURL(email: String): Future[Option[String]] = {
    Future.successful(Some("url"));
  }
}

