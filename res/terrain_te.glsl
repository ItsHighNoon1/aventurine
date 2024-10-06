#version 410 core

layout (quads, equal_spacing, ccw) in;

uniform mat4 u_mvpMatrix;
uniform sampler2D u_heightmapSampler;

in vec2 v_textureCoords[];

out float v_height;

void main() {
  float u = gl_TessCoord.x;
  float v = gl_TessCoord.y;
  
  vec2 t00 = v_textureCoords[0];
  vec2 t01 = v_textureCoords[1];
  vec2 t10 = v_textureCoords[2];
  vec2 t11 = v_textureCoords[3];
  
  vec2 t0 = (t01 - t00) * u + t00;
  vec2 t1 = (t11 - t10) * u + t10;
  vec2 texCoords = (t1 - t0) * v + t0;
  
  vec4 p00 = gl_in[0].gl_Position;
  vec4 p01 = gl_in[1].gl_Position;
  vec4 p10 = gl_in[2].gl_Position;
  vec4 p11 = gl_in[3].gl_Position;
  
  vec4 p0 = (p01 - p00) * u + p00;
  vec4 p1 = (p11 - p10) * u + p10;
  vec4 position = (p1 - p0) * v + p0;

  vec4 heightmapColor = texture(u_heightmapSampler, texCoords);
  v_height = heightmapColor.r * 65536 + heightmapColor.g * 256 + heightmapColor.b;
  position.z = v_height;
  
  gl_Position = u_mvpMatrix * position;
}