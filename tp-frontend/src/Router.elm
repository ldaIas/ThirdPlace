module Router exposing (Route(..), parseUrl)

import Url exposing (Url)
import Url.Parser exposing (..)

type Route
    = Login
    | Room
    | NotFound

parseUrl : Url -> Route
parseUrl url =
    case parse matchRoute url of
        Just route ->
            route

        Nothing ->
            NotFound

matchRoute : Parser (Route -> a) a
matchRoute =
    oneOf
        [ map Login top
        , map Login (s "login")
        , map Room (s "room")
        ]
