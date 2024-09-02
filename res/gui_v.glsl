#version 330 core

layout(location = 0) in vec2 a_position;
layout(location = 1) in vec2 a_texCoords;

out vec2 v_texCoords;
out vec3 v_normal;

uniform mat4 u_mMatrix;

void main() {
  v_texCoords = a_texCoords;
  gl_Position = u_mMatrix * vec4(a_position.x, a_position.y, 0.0, 1.0);
}