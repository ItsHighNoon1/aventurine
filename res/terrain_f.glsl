#version 410 core

in float v_height;

out vec4 out_color;

void main() {
  out_color = vec4(v_height / 500, 0.0, 0.0, 1.0);
}