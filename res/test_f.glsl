#version 330 core

in vec3 v_position;
in vec2 v_texCoords;
in vec3 v_normal;

out vec4 out_color;

uniform sampler2D u_diffuseSampler;

void main() {
  out_color = texture(u_diffuseSampler, v_texCoords);
}