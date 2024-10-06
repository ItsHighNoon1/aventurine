#version 410 core

layout(vertices = 4) out;

in vec2 v_texCoords[];

out vec2 v_textureCoords[];

void main() {
  gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
  v_textureCoords[gl_InvocationID] = v_texCoords[gl_InvocationID];
  
  if (gl_InvocationID == 0) {
    gl_TessLevelOuter[0] = 512;
    gl_TessLevelOuter[1] = 512;
    gl_TessLevelOuter[2] = 512;
    gl_TessLevelOuter[3] = 512;

    gl_TessLevelInner[0] = 512;
    gl_TessLevelInner[1] = 512;
  }
}