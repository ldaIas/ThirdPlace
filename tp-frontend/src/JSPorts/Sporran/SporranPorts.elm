port module JSPorts.Sporran.SporranPorts exposing (detectSporran, onSporranDetected, requestLogin, onLoginAttempted, onLoginSuccess)

port detectSporran : () -> Cmd msg
port onSporranDetected : (Bool -> msg) -> Sub msg

port requestLogin : () -> Cmd msg
port onLoginAttempted : (() -> msg) -> Sub msg

-- The user's DID is expected to come through
port onLoginSuccess : (String -> msg) -> Sub msg