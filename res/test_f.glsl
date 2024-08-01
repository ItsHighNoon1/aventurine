#version 330 core

in vec3 v_position;

out vec4 out_color;

void main() {
  out_color = vec4(v_position + 0.5, 1.0);
}