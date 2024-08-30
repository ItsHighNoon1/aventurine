#version 330 core

in vec3 v_position;
in vec3 v_normal;

out vec4 out_color;

void main() {
  out_color = vec4(v_normal / 2.0 + 0.5, 1.0);
}