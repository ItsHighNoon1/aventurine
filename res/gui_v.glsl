#version 330 core

layout(location = 0) in vec2 a_position;
layout(location = 1) in vec2 a_texCoords;

out vec2 v_texCoords;

uniform mat4 u_mMatrix;
uniform vec2 u_texOffset;
uniform vec2 u_texSize;

void main() {
  v_texCoords = u_texOffset + a_texCoords * u_texSize;
  gl_Position = u_mMatrix * vec4(a_position.x, a_position.y, 0.0, 1.0);
}