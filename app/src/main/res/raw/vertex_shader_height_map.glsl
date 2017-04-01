precision mediump float;
attribute vec3 vp;

uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;

uniform int nbStrips;
uniform int nbSlices;

uniform sampler2D textureHeight;

varying vec3 normal;
varying float z;

varying vec3 v_Position;

void main () {
  float coefficient = 0.2;

  vec4 textHeightColor = texture2D(textureHeight, vp.xz * vec2(2048.0));
  vec3 newVp = vec3(vp.x, coefficient * textHeightColor.r, vp.z);

  float x1 = newVp.x + 1.0 / float(nbSlices);
  float z1 = newVp.z + 1.0 / float(nbStrips);
  vec3 vp1 = vec3(x1, coefficient * texture2D(textureHeight, vec2(x1, newVp.z) * vec2(2048.0)).r, newVp.z);
  vec3 vp2 = vec3(newVp.x, coefficient * texture2D(textureHeight, vec2(newVp.x, z1) * vec2(2048.0)).r, z1);
  normal = vec3(u_MVMatrix * vec4(normalize(cross(vp2 - newVp, vp1 - newVp)), 0.0));
  newVp = vec3(u_MVMatrix * vec4(newVp, 1.0));
  v_Position = newVp;
  z = newVp.y;

  gl_Position = u_MVPMatrix * vec4 (newVp, 1.0);
}