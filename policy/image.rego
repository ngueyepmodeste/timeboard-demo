package main

# Refuser si l'image n'est pas définie
deny[msg] {
  not input.image
  msg := "image is not defined"
}

# Refuser les tags 'latest'
deny[msg] {
  img := input.image
  splitted := split(img, ":")
  count(splitted) == 2
  tag := splitted[1]
  tag == "latest"
  msg := sprintf("image tag 'latest' is not allowed: %s", [img])
}

# Imposer le port 8080 à l'intérieur du conteneur
deny[msg] {
  input.container_port != 8080
  msg := sprintf("container_port must be 8080, got %v", [input.container_port])
}
