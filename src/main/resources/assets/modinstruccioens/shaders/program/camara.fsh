#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    // Coordenadas perfectas (Anti-Deformación)
    vec2 min_bounds = vec2(0.52, 0.27);
    vec2 max_bounds = vec2(0.98, 0.73);

    // Usamos >= y <= para evitar artefactos transparentes de 1 píxel en los bordes
    if (texCoord.x >= min_bounds.x && texCoord.x <= max_bounds.x &&
        texCoord.y >= min_bounds.y && texCoord.y <= max_bounds.y) {
        
        vec2 tamano = max_bounds - min_bounds;
        vec2 uv_comprimido = (texCoord - min_bounds) / tamano;
        
        // Leemos la textura directamente
        fragColor = vec4(texture(DiffuseSampler, uv_comprimido).rgb, 1.0);
    } else {
        // Fondo negro sólido
        fragColor = vec4(0.0, 0.0, 0.0, 1.0); 
    }
}