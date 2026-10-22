import os

css = open('app/src/main/assets/leaflet.css').read()
js = open('app/src/main/assets/leaflet.js').read()

html_code = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <title>WAYPOINT Interactive Map</title>
  <style>
/* Leaflet Inlined CSS */
{css}

/* Custom Waypoint Styling */
* {{
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  box-sizing: border-box;
}}

html, body {{
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  background-color: #0E1524;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Inter', Roboto, sans-serif;
  overflow: hidden;
}}

#map-container {{
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 100%;
  background-color: #0F172A;
}}

#map {{
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
}}

/* Vector Base Layer - Never Blank */
#vector-fallback {{
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(circle at 50% 50%, #1E293B 0%, #0F172A 100%);
  overflow: hidden;
}}

/* Google Dark Filter */
.google-dark-tiles {{
  filter: invert(90%) hue-rotate(180deg) brightness(85%) contrast(110%);
}}

/* Real Google Maps Watermark */
.google-watermark {{
  position: absolute;
  bottom: 14px;
  left: 14px;
  z-index: 800;
  font-family: 'Roboto', -apple-system, sans-serif;
  font-weight: 700;
  font-size: 15px;
  letter-spacing: -0.3px;
  background: rgba(255, 255, 255, 0.95);
  padding: 3px 9px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  pointer-events: none;
  display: flex;
  align-items: center;
  gap: 1px;
}}

/* Real Dropped Pin Marker */
.dropped-pin {{
  position: relative;
  width: 32px;
  height: 42px;
  cursor: pointer;
}}
.dropped-pin-icon {{
  width: 30px;
  height: 40px;
  background: #EF4444;
  border: 2.5px solid #FFFFFF;
  border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg);
  box-shadow: 0 4px 14px rgba(239, 68, 68, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
}}
.dropped-pin-icon::after {{
  content: '';
  width: 10px;
  height: 10px;
  background: #FFFFFF;
  border-radius: 50%;
  transform: rotate(45deg);
}}
.dropped-pin-pulse {{
  position: absolute;
  bottom: -4px;
  left: 7px;
  width: 16px;
  height: 8px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  filter: blur(1px);
}}

/* Start / Origin Ring */
.origin-ring {{
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #090D14;
  border: 4px solid #10B981;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.6), 0 0 14px rgba(16, 185, 129, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
}}
.origin-ring::after {{
  content: '';
  width: 8px;
  height: 8px;
  background: #10B981;
  border-radius: 50%;
}}

/* Destination Ring */
.dest-ring {{
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #090D14;
  border: 4px solid #3B82F6;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.6), 0 0 14px rgba(59, 130, 246, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
}}
.dest-ring::after {{
  content: '';
  width: 8px;
  height: 8px;
  background: #FFFFFF;
  border-radius: 50%;
}}

/* Translucent hazard callout */
.hazard-callout {{
  background: rgba(220, 38, 38, 0.94);
  color: #FFFFFF;
  font-weight: 700;
  font-size: 11px;
  letter-spacing: -0.1px;
  padding: 5px 12px;
  border-radius: 14px;
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  white-space: nowrap;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.5);
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transform: translate(-30%, -125%);
  cursor: pointer;
}}

/* Facility bead */
.facility-bubble {{
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #0F172A;
  border: 2px solid #38BDF8;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5), 0 0 10px rgba(56, 189, 248, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.15s ease;
}}
.facility-bubble:active {{
  transform: scale(0.92);
}}
.facility-bubble.water {{
  border-color: #0284C7;
  background: #0C4A6E;
}}
.facility-bubble.shade {{
  border-color: #10B981;
  background: #064E3B;
}}
.facility-bubble.mist {{
  border-color: #38BDF8;
  background: #082F49;
}}
.facility-bubble.restroom {{
  border-color: #A855F7;
  background: #3B0764;
}}

/* Street corridor labels */
.corridor-label {{
  background: rgba(6, 78, 59, 0.92);
  color: #6EE7B7;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  padding: 3px 8px;
  border-radius: 6px;
  border: 1px solid #10B981;
  box-shadow: 0 2px 6px rgba(0,0,0,0.4);
  white-space: nowrap;
  pointer-events: none;
}}
  </style>
</head>
<body>
  <div id="map-container">
    <!-- Vector Baseline Ground Plan -->
    <div id="vector-fallback">
      <svg width="100%" height="100%" viewBox="0 0 800 600" preserveAspectRatio="xMidYMid slice" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
            <path d="M 40 0 L 0 0 0 40" fill="none" stroke="rgba(255,255,255,0.04)" stroke-width="1"/>
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="#0F172A"/>
        <rect width="100%" height="100%" fill="url(#grid)"/>

        <!-- Street Network Background (Msheireb Downtown Doha) -->
        <!-- Grand Hamad Avenue -->
        <path d="M 720 0 L 680 600" stroke="#1E293B" stroke-width="44" stroke-linecap="round" fill="none"/>
        <!-- Al Rayyan Road / Jasim Bin Mohammed -->
        <path d="M 0 160 L 800 130" stroke="#1E293B" stroke-width="38" stroke-linecap="round" fill="none"/>
        <!-- Al Kahraba Street -->
        <path d="M 280 140 L 320 540" stroke="#334155" stroke-width="28" stroke-linecap="round" fill="none"/>
        <path d="M 280 140 L 320 540" stroke="#475569" stroke-width="2" stroke-dasharray="6 6" fill="none"/>
        <!-- Abdullah Bin Thani Street -->
        <path d="M 440 140 L 470 540" stroke="#334155" stroke-width="24" stroke-linecap="round" fill="none"/>

        <!-- Sikkat Al Wadi Shaded Pedestrian Spine -->
        <path d="M 360 150 L 390 530" stroke="rgba(16, 185, 129, 0.45)" stroke-width="32" stroke-linecap="round" fill="none"/>
        <path d="M 360 150 L 390 530" stroke="#10B981" stroke-width="4" stroke-dasharray="10 4" fill="none"/>

        <!-- Souq Waqif Pedestrian Alleyways -->
        <path d="M 550 140 L 700 135 L 710 400 L 560 390 Z" fill="rgba(217, 119, 6, 0.12)" stroke="#D97706" stroke-width="1.5" stroke-dasharray="4 4"/>

        <!-- Barahat Msheireb Central Covered Square -->
        <rect x="330" y="280" width="120" height="100" rx="16" fill="rgba(6, 78, 59, 0.35)" stroke="#10B981" stroke-width="2"/>
        <text x="390" y="335" font-family="sans-serif" font-size="12" font-weight="bold" fill="#6EE7B7" text-anchor="middle">BARAHAT MSHEIREB</text>

        <!-- Building Blocks -->
        <rect x="160" y="200" width="90" height="80" rx="8" fill="#1E293B" stroke="#334155" stroke-width="1.5"/>
        <rect x="170" y="320" width="80" height="120" rx="8" fill="#1E293B" stroke="#334155" stroke-width="1.5"/>
        <rect x="490" y="200" width="70" height="100" rx="8" fill="#1E293B" stroke="#334155" stroke-width="1.5"/>
        <rect x="500" y="330" width="65" height="90" rx="8" fill="#1E293B" stroke="#334155" stroke-width="1.5"/>

        <!-- Metro Station Plazas -->
        <circle cx="240" cy="150" r="26" fill="#B91C1C" fill-opacity="0.3" stroke="#EF4444" stroke-width="2"/>
        <text x="240" y="155" font-family="sans-serif" font-size="11" font-weight="bold" fill="#FFFFFF" text-anchor="middle">🚇 METRO</text>

        <circle cx="630" cy="220" r="24" fill="#B91C1C" fill-opacity="0.3" stroke="#EF4444" stroke-width="2"/>
        <text x="630" y="225" font-family="sans-serif" font-size="10" font-weight="bold" fill="#FFFFFF" text-anchor="middle">SOUQ</text>
      </svg>
    </div>

    <!-- Leaflet Live Map Viewport -->
    <div id="map"></div>
  </div>

  <!-- Real Google Maps Watermark -->
  <div class="google-watermark" id="googleWatermark">
    <span style="color:#4285F4">G</span><span style="color:#EA4335">o</span><span style="color:#FBBC05">o</span><span style="color:#4285F4">g</span><span style="color:#34A853">l</span><span style="color:#EA4335">e</span>
  </div>

  <script>
/* Leaflet JS Inlined */
{js}
  </script>

  <script>
    let map = null;
    let currentTileLayer = null;
    let currentStyle = 'google_streets';
    let markersLayer = null;
    let routesLayer = null;
    let labelsLayer = null;
    let originMarker = null;
    let destMarker = null;
    let droppedPinMarker = null;

    // Real Live Tile Providers: Google Maps, Satellite, Dark Mode, OSM
    const TILE_PROVIDERS = {{
      google_streets: {{
        url: 'https://mt{{s}}.google.com/vt/lyrs=m&hl=en&x={{x}}&y={{y}}&z={{z}}',
        fallbackUrl: 'https://tile.openstreetmap.org/{{z}}/{{x}}/{{y}}.png',
        options: {{
          subdomains: '0123',
          maxZoom: 20,
          attribution: '&copy; Google Maps'
        }}
      }},
      google_satellite: {{
        url: 'https://mt{{s}}.google.com/vt/lyrs=y&hl=en&x={{x}}&y={{y}}&z={{z}}',
        fallbackUrl: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{{z}}/{{y}}/{{x}}',
        options: {{
          subdomains: '0123',
          maxZoom: 20,
          attribution: '&copy; Google Maps'
        }}
      }},
      google_dark: {{
        url: 'https://mt{{s}}.google.com/vt/lyrs=m&hl=en&x={{x}}&y={{y}}&z={{z}}',
        fallbackUrl: 'https://{{s}}.basemaps.cartocdn.com/dark_all/{{z}}/{{x}}/{{y}}{{r}}.png',
        options: {{
          subdomains: '0123',
          className: 'google-dark-tiles',
          maxZoom: 20,
          attribution: '&copy; Google Maps'
        }}
      }},
      osm: {{
        url: 'https://tile.openstreetmap.org/{{z}}/{{x}}/{{y}}.png',
        options: {{
          maxZoom: 19,
          attribution: '&copy; OpenStreetMap'
        }}
      }}
    }};

    function initMap(lat, lng, zoom) {{
      if (map) {{
        map.invalidateSize();
        return;
      }}
      try {{
        const centerLat = lat || 25.2870;
        const centerLng = lng || 51.5335;
        const initialZoom = zoom || 16;

        const mapEl = document.getElementById('map');
        if (!mapEl) return;

        map = L.map('map', {{
          zoomControl: false,
          attributionControl: false,
          fadeAnimation: true,
          zoomAnimation: true,
          preferCanvas: true
        }}).setView([centerLat, centerLng], initialZoom);

        // Apply Google Maps primary basemap
        setMapStyle(currentStyle || 'google_streets');

        markersLayer = L.layerGroup().addTo(map);
        routesLayer = L.layerGroup().addTo(map);
        labelsLayer = L.layerGroup().addTo(map);

        map.on('click', function(e) {{
          setDroppedPin(e.latlng.lat, e.latlng.lng);
          if (window.WaypointBridge && window.WaypointBridge.onMapClicked) {{
            window.WaypointBridge.onMapClicked(e.latlng.lat, e.latlng.lng);
          }}
        }});

        addCorridorLabels();

        // Staggered size invalidation to guarantee full container rendering
        [50, 150, 300, 600, 1200].forEach(delay => {{
          setTimeout(() => {{
            if (map) map.invalidateSize();
          }}, delay);
        }});

        if (window.WaypointBridge && window.WaypointBridge.onMapReady) {{
          window.WaypointBridge.onMapReady();
        }}
      }} catch (err) {{
        console.error('Map initialization failed:', err);
      }}
    }}

    function setMapStyle(styleName) {{
      if (!map) return;
      let key = 'google_streets';
      if (styleName === 'satellite' || styleName === 'google_satellite') {{
        key = 'google_satellite';
      }} else if (styleName === 'dark' || styleName === 'google_dark') {{
        key = 'google_dark';
      }} else if (styleName === 'osm') {{
        key = 'osm';
      }} else {{
        key = 'google_streets';
      }}

      currentStyle = key;
      const config = TILE_PROVIDERS[key] || TILE_PROVIDERS.google_streets;

      if (currentTileLayer) {{
        map.removeLayer(currentTileLayer);
      }}

      currentTileLayer = L.tileLayer(config.url, config.options);

      if (config.fallbackUrl) {{
        let fallbackAdded = false;
        currentTileLayer.on('tileerror', function() {{
          if (!fallbackAdded && map) {{
            fallbackAdded = true;
            console.warn('Primary tile failed, activating fallback layer');
            const fallbackLayer = L.tileLayer(config.fallbackUrl, {{ maxZoom: 19 }});
            map.removeLayer(currentTileLayer);
            currentTileLayer = fallbackLayer;
            fallbackLayer.addTo(map);
          }}
        }});
      }}

      currentTileLayer.addTo(map);

      // Watermark visibility
      const watermark = document.getElementById('googleWatermark');
      if (watermark) {{
        watermark.style.display = (key.startsWith('google') ? 'flex' : 'none');
      }}
    }}

    function addCorridorLabels() {{
      if (!labelsLayer) return;
      const sikkatIcon = L.divIcon({{
        className: 'custom-div-icon',
        html: '<div class="corridor-label">🌿 SIKKAT AL WADI · SHADED SPINE</div>',
        iconAnchor: [100, 0]
      }});
      L.marker([25.2872, 51.5332], {{ icon: sikkatIcon, interactive: false }}).addTo(labelsLayer);
    }}

    function setCenter(lat, lng, zoom) {{
      if (map) {{
        map.setView([lat, lng], zoom || map.getZoom(), {{ animate: true }});
      }}
    }}

    function zoomIn() {{
      if (map) map.zoomIn();
    }}

    function zoomOut() {{
      if (map) map.zoomOut();
    }}

    function setDroppedPin(lat, lng) {{
      if (!map) return;
      clearDroppedPin();

      const pinIcon = L.divIcon({{
        className: 'custom-div-icon',
        html: '<div class="dropped-pin"><div class="dropped-pin-icon"></div><div class="dropped-pin-pulse"></div></div>',
        iconSize: [32, 42],
        iconAnchor: [16, 42]
      }});

      droppedPinMarker = L.marker([lat, lng], {{ icon: pinIcon, zIndexOffset: 1000 }}).addTo(map);

      const popupContent = `
        <div style="font-family: -apple-system, sans-serif; font-size: 13px; min-width: 170px; padding: 4px;">
          <div style="font-weight: 700; color: #0F172A; margin-bottom: 3px;">Dropped Waypoint</div>
          <div style="font-size: 11px; color: #64748B; margin-bottom: 8px;">${{lat.toFixed(4)}}, ${{lng.toFixed(4)}}</div>
          <div style="display: flex; gap: 6px;">
            <button onclick="window.WaypointBridge && window.WaypointBridge.onMapClicked(${{lat}}, ${{lng}});" style="flex:1; background: #10B981; color: white; border: none; padding: 5px 8px; border-radius: 6px; font-weight: 600; font-size: 11px; cursor: pointer;">Walk Here</button>
            <button onclick="clearDroppedPin();" style="background: #E2E8F0; color: #334155; border: none; padding: 5px 8px; border-radius: 6px; font-weight: 600; font-size: 11px; cursor: pointer;">Clear</button>
          </div>
        </div>
      `;
      droppedPinMarker.bindPopup(popupContent, {{ closeButton: true, offset: [0, -32] }}).openPopup();
    }}

    function clearDroppedPin() {{
      if (droppedPinMarker && map) {{
        map.removeLayer(droppedPinMarker);
        droppedPinMarker = null;
      }}
    }}

    function setRouteEndpoints(originLat, originLng, destLat, destLng) {{
      if (!map) return;
      if (originMarker) map.removeLayer(originMarker);
      if (destMarker) map.removeLayer(destMarker);

      if (originLat && originLng) {{
        const originIcon = L.divIcon({{
          className: 'custom-div-icon',
          html: '<div class="origin-ring"></div>',
          iconSize: [26, 26],
          iconAnchor: [13, 13]
        }});
        originMarker = L.marker([originLat, originLng], {{ icon: originIcon, zIndexOffset: 900 }}).addTo(map);
      }}

      if (destLat && destLng) {{
        const destIcon = L.divIcon({{
          className: 'custom-div-icon',
          html: '<div class="dest-ring"></div>',
          iconSize: [26, 26],
          iconAnchor: [13, 13]
        }});
        destMarker = L.marker([destLat, destLng], {{ icon: destIcon, zIndexOffset: 900 }}).addTo(map);
      }}
    }}

    function setMarkers(facilities, barriers) {{
      if (!map || !markersLayer) return;
      markersLayer.clearLayers();

      const facList = typeof facilities === 'string' ? JSON.parse(facilities) : facilities;
      const barList = typeof barriers === 'string' ? JSON.parse(barriers) : barriers;

      if (Array.isArray(facList)) {{
        facList.forEach(fac => {{
          let emoji = '💧';
          let cls = 'water';
          if (fac.category === 'SHADE_SHELTER') {{ emoji = '🏠'; cls = 'shade'; }}
          else if (fac.category === 'MISTING_STATION') {{ emoji = '❄️'; cls = 'mist'; }}
          else if (fac.category === 'RESTROOM') {{ emoji = '🚻'; cls = 'restroom'; }}
          else if (fac.category === 'ACCESSIBLE_RAMP') {{ emoji = '♿'; cls = 'shade'; }}

          const icon = L.divIcon({{
            className: 'custom-div-icon',
            html: `<div class="facility-bubble ${{cls}}">${{emoji}}</div>`,
            iconSize: [34, 34],
            iconAnchor: [17, 17]
          }});

          const marker = L.marker([fac.lat, fac.lng], {{ icon: icon }});
          marker.on('click', function() {{
            if (window.WaypointBridge && window.WaypointBridge.onMarkerClicked) {{
              window.WaypointBridge.onMarkerClicked(fac.id, 'FACILITY');
            }}
          }});
          marker.addTo(markersLayer);
        }});
      }}

      if (Array.isArray(barList)) {{
        barList.forEach(bar => {{
          const icon = L.divIcon({{
            className: 'custom-div-icon',
            html: '<div class="hazard-callout">⚠️ OBSTACLE</div>',
            iconSize: [110, 28],
            iconAnchor: [14, 28]
          }});

          const marker = L.marker([bar.lat, bar.lng], {{ icon: icon, zIndexOffset: 850 }});
          marker.on('click', function() {{
            if (window.WaypointBridge && window.WaypointBridge.onMarkerClicked) {{
              window.WaypointBridge.onMarkerClicked(bar.id, 'BARRIER');
            }}
          }});
          marker.addTo(markersLayer);
        }});
      }}
    }}

    function displayRoutes(routesData, selectedIndex) {{
      if (!map || !routesLayer) return;
      routesLayer.clearLayers();

      try {{
        const routes = typeof routesData === 'string' ? JSON.parse(routesData) : routesData;
        if (!Array.isArray(routes) || routes.length === 0) return;

        routes.forEach((route, idx) => {{
          const isSelected = (idx === selectedIndex);

          if (isSelected) {{
            L.polyline(route.points, {{
              color: '#059669',
              weight: 10,
              opacity: 0.4,
              lineJoin: 'round',
              lineCap: 'round'
            }}).addTo(routesLayer);
          }}

          const polyline = L.polyline(route.points, {{
            color: isSelected ? '#10B981' : '#64748B',
            weight: isSelected ? 5.5 : 3.5,
            opacity: isSelected ? 1.0 : 0.65,
            dashArray: isSelected ? null : '4, 6',
            lineJoin: 'round',
            lineCap: 'round'
          }});

          polyline.on('click', function() {{
            if (window.WaypointBridge && window.WaypointBridge.onRouteClicked) {{
              window.WaypointBridge.onRouteClicked(idx);
            }}
          }});

          polyline.addTo(routesLayer);
        }});

        const activeRoute = routes[selectedIndex] || routes[0];
        if (activeRoute && activeRoute.points && activeRoute.points.length > 0) {{
          const startPt = activeRoute.points[0];
          const endPt = activeRoute.points[activeRoute.points.length - 1];
          setRouteEndpoints(startPt[0], startPt[1], endPt[0], endPt[1]);

          if (map) {{
            map.invalidateSize();
            try {{
              const bounds = L.latLngBounds(activeRoute.points);
              if (bounds.isValid()) {{
                map.fitBounds(bounds, {{ padding: [60, 60], maxZoom: 17 }});
              }}
            }} catch (err) {{
              console.warn('fitBounds error', err);
            }}
          }}
        }}
      }} catch (e) {{
        console.error('Failed to parse routes', e);
      }}
    }}

    function clearRoutes() {{
      if (routesLayer) routesLayer.clearLayers();
    }}

    function invalidateMapSize() {{
      if (map) {{
        map.invalidateSize();
      }}
    }}

    window.addEventListener('resize', () => {{
      invalidateMapSize();
    }});

    // Modern ResizeObserver: triggers whenever container size updates
    if (window.ResizeObserver) {{
      const ro = new ResizeObserver(() => {{
        if (map) map.invalidateSize();
      }});
      const c = document.getElementById('map-container');
      if (c) ro.observe(c);
    }}

    // Auto-init immediately upon script execution
    if (document.readyState === 'complete' || document.readyState === 'interactive') {{
      setTimeout(() => initMap(25.2870, 51.5335, 16), 10);
    }} else {{
      window.addEventListener('DOMContentLoaded', () => initMap(25.2870, 51.5335, 16));
    }}
  </script>
</body>
</html>
"""

with open('app/src/main/assets/leaflet_map.html', 'w') as f:
    f.write(html_code)

print('Generated leaflet_map.html successfully. Size:', len(html_code))
