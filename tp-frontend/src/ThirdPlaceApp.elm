module ThirdPlaceApp exposing (init, main, subscriptions, update, view)

import Browser
import Browser.Navigation as Navigation
import JSPorts.Geohash.GeohashHandler as GeohashHandler
import JSPorts.Sporran.SporranHandler as SporranHandler
import ThirdPlaceModel exposing (Model, Msg(..))
import Url exposing (Url)
import Views.Login.LoginView exposing (view)
import Views.Room.RoomHandler as RoomHandler
import Views.Room.RoomModel as RoomModel
import Views.Room.RoomView exposing (view)
import Views.ThirdPlaceAppView

init : flags -> Url -> Navigation.Key -> ( Model, Cmd Msg )
init _ url key =
    let
        sporranInit : ( SporranHandler.Model, Cmd SporranHandler.Msg )
        sporranInit =
            SporranHandler.init

        geohashInit : ( GeohashHandler.Model, Cmd GeohashHandler.Msg )
        geohashInit =
            ( GeohashHandler.init, Cmd.none )

        roomInit : ( RoomModel.Model, Cmd RoomModel.Msg )
        roomInit =
            RoomHandler.init

        ( sporranModel, sporranCmd ) =
            sporranInit

        ( geohashModel, geohashCmd ) =
            geohashInit

        ( roomModel, roomCmd ) =
            roomInit

        loginModel : Model
        loginModel =
            { pageKey = key
            , pageUrl = url
            , authenticated = False
            , sporranHandler = sporranModel
            , geohashHandler = geohashModel
            , roomHandler = roomModel
            }
    in
    ( loginModel
    , Cmd.batch
        [ Cmd.map SporranMsg sporranCmd
        , Cmd.map GeohashMsg geohashCmd
        , Cmd.map RoomMsg roomCmd
        ]
    )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SporranMsg sporranMsg ->
            let
                ( updatedSporranModel, cmd ) =
                    SporranHandler.update sporranMsg model.sporranHandler

                -- If the sporran model has a user did, we are authenticated
                authenticated : Bool
                authenticated =
                    case updatedSporranModel.userDid of
                        Just _ ->
                            True

                        Nothing ->
                            False
            in
            ( { model | sporranHandler = updatedSporranModel, authenticated = authenticated }, Cmd.map SporranMsg cmd )

        GeohashMsg geohashMsg ->
            let
                ( updatedGeohashModel, cmd ) =
                    GeohashHandler.update model.geohashHandler geohashMsg

                -- Update the room model to have the new room id
                updatedRoomModel =
                    let
                        roomModel = model.roomHandler
                    in
                    { roomModel | roomId = updatedGeohashModel.roomId}
            in
            ( { model | geohashHandler = updatedGeohashModel, roomHandler = updatedRoomModel }, Cmd.map GeohashMsg cmd )

        RoomMsg roomMsg ->
            let
                ( updatedRoomModel, cmd ) =
                    RoomHandler.update roomMsg model.roomHandler
            in
            ( { model | roomHandler = updatedRoomModel }, Cmd.map RoomMsg cmd )

        UrlChanged _ ->
            ( model, Cmd.none )

        NoOp ->
            ( model, Cmd.none )


view : Model -> Browser.Document Msg
view model =
    Views.ThirdPlaceAppView.view model


main : Program () Model Msg
main =
    Browser.application
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        , onUrlChange = UrlChanged
        , onUrlRequest = \_ -> NoOp
        }



{-
   -| Subscriptions for the various events in the app
   -| These are things like user DID generation and authentication
-}


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.batch
        [ Sub.map SporranMsg (SporranHandler.subscriptions model.sporranHandler)
        , Sub.map GeohashMsg (GeohashHandler.subscriptions model.geohashHandler)
        ]
