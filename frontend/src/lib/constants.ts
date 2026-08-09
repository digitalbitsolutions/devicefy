export const TIPOS_EQUIPO = ['CPU', 'ESTACION_TRABAJO', 'PORTATIL', 'SERVIDOR', 'THIN_CLIENT'] as const

export const ESTADOS_EQUIPO = ['PENDIENTE', 'EN_PROCESO', 'FINALIZADO', 'BAJA'] as const

export const ESTADOS_PROYECTO = ['PENDIENTE', 'EN_PROCESO', 'FINALIZADO'] as const

export const TIPOS_ASIGNACION_RED = ['DHCP', 'ESTATICA'] as const

export const TIPOS_PERIFERICO = ['MONITOR', 'IMPRESORA', 'TECLADO', 'RATON', 'OTRO'] as const

export const COMUNIDADES_AUTONOMAS = [
  'Andalucía',
  'Aragón',
  'Asturias',
  'Islas Baleares',
  'Islas Canarias',
  'Cantabria',
  'Castilla-La Mancha',
  'Castilla y León',
  'Cataluña',
  'Comunidad Valenciana',
  'Extremadura',
  'Galicia',
  'La Rioja',
  'Comunidad de Madrid',
  'Región de Murcia',
  'Comunidad Foral de Navarra',
  'País Vasco',
  'Ciudad de Ceuta',
  'Ciudad de Melilla',
] as const

export const PROVINCIAS_POR_CCAA: Record<string, string[]> = {
  'Andalucía': ['Almería', 'Cádiz', 'Córdoba', 'Granada', 'Huelva', 'Jaén', 'Málaga', 'Sevilla'],
  'Aragón': ['Huesca', 'Teruel', 'Zaragoza'],
  'Asturias': ['Asturias'],
  'Islas Baleares': ['Islas Baleares'],
  'Islas Canarias': ['Las Palmas', 'Santa Cruz de Tenerife'],
  'Cantabria': ['Cantabria'],
  'Castilla-La Mancha': ['Albacete', 'Ciudad Real', 'Cuenca', 'Guadalajara', 'Toledo'],
  'Castilla y León': ['Ávila', 'Burgos', 'León', 'Palencia', 'Salamanca', 'Segovia', 'Soria', 'Valladolid', 'Zamora'],
  'Cataluña': ['Barcelona', 'Girona', 'Lleida', 'Tarragona'],
  'Comunidad Valenciana': ['Alicante', 'Castellón', 'Valencia'],
  'Extremadura': ['Badajoz', 'Cáceres'],
  'Galicia': ['A Coruña', 'Lugo', 'Ourense', 'Pontevedra'],
  'La Rioja': ['La Rioja'],
  'Comunidad de Madrid': ['Madrid'],
  'Región de Murcia': ['Murcia'],
  'Comunidad Foral de Navarra': ['Navarra'],
  'País Vasco': ['Álava', 'Bizkaia', 'Gipuzkoa'],
  'Ciudad de Ceuta': ['Ceuta'],
  'Ciudad de Melilla': ['Melilla'],
}
