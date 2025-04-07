port module JSPorts.Sporran.SporranPorts exposing (detectSporran, onSporranDetected, requestLogin, onLoginSuccess)

port detectSporran : () -> Cmd msg
port onSporranDetected : (Bool -> msg) -> Sub msg

port requestLogin : () -> Cmd msg
port onLoginSuccess : (String -> msg) -> Sub msg