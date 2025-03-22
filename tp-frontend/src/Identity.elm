module Identity exposing (Model, Msg(..), init, update, subscriptions)

import Json.Decode exposing (Decoder, field, string)
import Utils.Ports exposing (didGenerated)

type alias Model =
    { did : Maybe String
    , privKey : Maybe String
    , pubKey : Maybe String
    }

type Msg
    = RequestDID
    | DIDGenerated String String String  -- (did, privKey, pubKey)

init : ( Model, Cmd Msg )
init =
    ( { did = Nothing, privKey = Nothing, pubKey = Nothing }
    , Cmd.none
    )

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        RequestDID ->
            ( model, Ports.generateDID () )

        DIDGenerated did privKey pubKey ->
            ( { model | did = Just did, privKey = Just privKey, pubKey = Just pubKey }
            , Cmd.none
            )

didDecoder : Decoder Msg
didDecoder =
    Json.Decode.map3 DIDGenerated
        (field "did" string)
        (field "privKey" string)
        (field "pubKey" string)

subscriptions : Model -> Sub Msg
subscriptions _ =
    didGenerated didDecoder
