#version 330 core

layout(location = 0) in vec3 a_position;
layout(location = 1) in vec2 a_texCoords;
layout(location = 2) in vec3 a_normal;

out vec3 v_position;
out vec3 v_normal;

uniform mat4 u_mvpMatrix;

void main() {
  v_position = a_position;
  v_normal = a_normal;
  gl_Position = u_mvpMatrix * vec4(a_position, 1.0);
}