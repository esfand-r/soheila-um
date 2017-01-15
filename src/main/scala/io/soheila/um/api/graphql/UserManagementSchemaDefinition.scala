//package com.soheila.um.api.graphql
//
//import com.mohiva.play.silhouette.api.LoginInfo
//import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
//import io.soheila.commons.entities.Attribute
//import io.soheila.commons.geospatials.Coordinate
//import com.soheila.um.entities.{ ContactInfo, NotificationSettings, User }
//import com.soheila.um.types.{ NotificationMethod, UserRole }
//import com.soheila.um.vos.{ UserCreationVO, UserUpdateVO }
//import sangria.schema.{ Argument, BooleanType, EnumType, EnumValue, Field, FloatType, InputField, InputObjectType, ListType, ObjectType, OptionInputType, OptionType, StringType, fields, interfaces }
//import sangria.macros.derive._
//import sangria.marshalling.{ CoercedScalaResultMarshaller, FromInput }
//
//object UserManagementSchemaDefinition {
//  implicit val UserRoleEnum = EnumType(
//    "UserRole",
//    Some("Types of UserRole"),
//    List(
//      EnumValue(
//        "Guest",
//        value = UserRole.Guest,
//        description = Some("")
//      ),
//      EnumValue(
//        "SimpleUser",
//        value = UserRole.SimpleUser,
//        description = Some("")
//      ),
//      EnumValue(
//        "Admin",
//        value = UserRole.Admin,
//        description = Some("")
//      )
//    )
//  )
//
//  val NotificationMethodEnum = EnumType(
//    "NotificationMethod",
//    Some("Types of NotificationMethod"),
//    List(
//      EnumValue(
//        "Email",
//        value = NotificationMethod.Email,
//        description = Some("")
//      ),
//      EnumValue(
//        "SMS",
//        value = NotificationMethod.SMS,
//        description = Some("")
//      )
//    )
//  )
//
//  val LoginInfoType =
//    ObjectType(
//      "LoginInfo",
//      "LoginInfo of the login provider",
//      interfaces[Unit, LoginInfo](),
//      fields[Unit, LoginInfo](
//        Field("providerID", StringType,
//          Some("The providerID of the login."),
//          resolve = _.value.providerID),
//        Field("providerKey", StringType,
//          Some("The providerKey of the login."),
//          resolve = _.value.providerKey)
//      )
//    )
//
//  val CoordinateType =
//    ObjectType(
//      "Coordinate",
//      "Coordinate of a user",
//      interfaces[Unit, Coordinate](),
//      fields[Unit, Coordinate](
//        Field("lon", FloatType,
//          Some(""),
//          resolve = _.value.lon),
//        Field("lat", FloatType,
//          Some(""),
//          resolve = _.value.lat)
//      )
//    )
//
//  val ContactInfoType =
//    ObjectType(
//      "ContactInfo",
//      "",
//      interfaces[Unit, ContactInfo](),
//      fields[Unit, ContactInfo](
//        Field("city", StringType,
//          Some(""),
//          resolve = _.value.city),
//        Field("country", StringType,
//          Some(""),
//          resolve = _.value.country),
//        Field("isBusiness", BooleanType,
//          Some(""),
//          resolve = _.value.isBusiness),
//        Field("isPrimary", BooleanType,
//          Some(""),
//          resolve = _.value.isPrimary),
//        Field("name", StringType,
//          Some(""),
//          resolve = _.value.name),
//        Field("phoneNumbers", ListType(StringType),
//          Some(""),
//          resolve = _.value.phoneNumbers.toSeq),
//        Field("postcode", StringType,
//          Some(""),
//          resolve = _.value.postcode),
//        Field("state", StringType,
//          Some(""),
//          resolve = _.value.state)
//      )
//    )
//
//  val NotificationSettingsType =
//    ObjectType(
//      "NotificationSettings",
//      "NotificationSettings of a user",
//      interfaces[Unit, NotificationSettings](),
//      fields[Unit, NotificationSettings](
//        Field("notificationMethod", NotificationMethodEnum,
//          Some(""),
//          resolve = _.value.notificationMethod),
//        Field("lat", BooleanType,
//          Some(""),
//          resolve = _.value.notifyWhenProfileUpdated),
//        Field("lat", BooleanType,
//          Some(""),
//          resolve = _.value.notifyWhenLoggedInFromNewDevice)
//      )
//    )
//
//  val AttributeType = ObjectType(
//    "AttributeSchema",
//    "Attribute Schema",
//    interfaces[Unit, Attribute](),
//    fields[Unit, Attribute](
//      Field("key", StringType, Some("Dynamic attribute key."), resolve = _.value.key),
//      Field("value", ListType(StringType), Some("Dynamic attribute value."), resolve = _.value.value)
//    )
//  )
//
//  val User =
//    ObjectType(
//      "User",
//      "A user in Soheila UM.",
//      interfaces[Unit, User](),
//      fields[Unit, User](
//        Field("uuid", OptionType(StringType),
//          Some("The primary ID of the user."),
//          resolve = _.value.uuid),
//        Field("loginInfo", LoginInfoType,
//          Some(""),
//          resolve = _.value.loginInfo),
//        Field("firstName", OptionType(StringType),
//          Some(""),
//          resolve = _.value.firstName),
//        Field("middleName", OptionType(StringType),
//          Some(""),
//          resolve = _.value.middleName),
//        Field("lastName", OptionType(StringType),
//          Some(""),
//          resolve = _.value.lastName),
//        Field("fullName", OptionType(StringType),
//          Some(""),
//          resolve = _.value.fullName),
//        Field("email", OptionType(StringType),
//          Some(""),
//          resolve = _.value.fullName),
//        Field("avatarURL", OptionType(StringType),
//          Some(""),
//          resolve = _.value.avatarURL),
//        Field("activated", BooleanType,
//          Some(""),
//          resolve = _.value.activated),
//        Field("createdOn", OptionType(LocalDateTimeType),
//          Some(""),
//          resolve = _.value.createdOn),
//        Field("updatedOn", OptionType(LocalDateTimeType),
//          Some(""),
//          resolve = _.value.updatedOn),
//        Field("roles", ListType(UserRoleEnum),
//          Some(""),
//          resolve = _.value.roles.toSeq),
//        Field("archived", BooleanType,
//          Some(""),
//          resolve = _.value.archived),
//        Field("coordinate", CoordinateType,
//          Some(""),
//          resolve = _.value.coordinate),
//        Field("contactInfo", OptionType(ContactInfoType),
//          Some(""),
//          resolve = _.value.contactInfo),
//        Field("notificationSettings", NotificationSettingsType,
//          Some(""),
//          resolve = _.value.notificationSettings),
//        Field("additionalContactInfo", ListType(ContactInfoType),
//          Some(""),
//          resolve = _.value.additionalContactInfo.toSeq),
//        Field("attributes", ListType(AttributeType),
//          Some(""),
//          resolve = _.value.attributes),
//        Field("language", StringType,
//          Some(""),
//          resolve = _.value.preferredLocale)
//      )
//    )
//
//  implicit val loginInfoInputType = deriveInputObjectType[LoginInfo](
//    InputObjectTypeName("LoginInfoVO")
//  )
//
//  val CommonSocialProfileInputType = InputObjectType[CommonSocialProfile]("content", "Content",
//    List(
//      InputField("loginInfo", loginInfoInputType),
//      InputField("firstName", StringType),
//      InputField("lastName", StringType),
//      InputField("fullName", StringType),
//      InputField("email", StringType),
//      InputField("avatarURL", StringType)
//    ))
//
//  implicit val commonSocialProfileFromInput = new FromInput[CommonSocialProfile] {
//    val marshaller = CoercedScalaResultMarshaller.default
//    def fromResult(node: marshaller.Node) = {
//      val ad = node.asInstanceOf[Map[String, Any]]
//
//      CommonSocialProfile(
//        loginInfo = ad("loginInfo").asInstanceOf[LoginInfo],
//        firstName = Some(ad("firstName").asInstanceOf[String]),
//        lastName = Some(ad("lastName").asInstanceOf[String]),
//        fullName = Some(ad("fullName").asInstanceOf[String]),
//        avatarURL = Some(ad("avatarURL").asInstanceOf[String])
//      )
//    }
//  }
//
//  implicit val UserCreationInputType = deriveInputObjectType[UserCreationVO](
//    InputObjectTypeName("UserCreationVO"),
//    InputObjectTypeDescription("A VO used to create a User entity"),
//    DocumentInputField("loginInfo", "Information specific to provider used for authentication")
//  )
//
//  implicit val UserUpdateInputType = deriveInputObjectType[UserUpdateVO](
//    InputObjectTypeName("UserUpdateVO"),
//    InputObjectTypeDescription("A VO used to update a User entity")
//  )
//
//  val UserRoleArg = Argument("userRole", OptionInputType(UserRoleEnum), description = "")
//}
