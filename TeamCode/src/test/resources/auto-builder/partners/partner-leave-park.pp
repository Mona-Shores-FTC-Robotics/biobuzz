{
  "startPoint": {
    "x": 24,
    "y": 132.25,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-slide-p-1",
      "color": "#3cc8e4",
      "name": "START to SLIDE_P",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 131
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-park-p-2",
      "color": "#3cc8e4",
      "name": "SLIDE_P to PARK_P",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 111
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-park-p2-3",
      "color": "#3cc8e4",
      "name": "PARK_P to PARK_P2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 110
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    }
  ],
  "shapes": [
    {
      "id": "frame-leg-red",
      "name": "HIVE frame leg (red side)",
      "vertices": [
        {
          "x": 46.6,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 90.3
        },
        {
          "x": 46.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "frame-leg-blue",
      "name": "HIVE frame leg (blue side)",
      "vertices": [
        {
          "x": 92.6,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 90.3
        },
        {
          "x": 92.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "blue-half",
      "name": "Blue half: stay out",
      "vertices": [
        {
          "x": 70.8,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 141.5
        },
        {
          "x": 70.8,
          "y": 141.5
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "to-slide-p-1"
    },
    {
      "kind": "path",
      "lineId": "to-park-p-2"
    },
    {
      "kind": "path",
      "lineId": "to-park-p2-3"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 18,
    "rHeight": 18,
    "safetyMargin": 1,
    "maxVelocity": 40,
    "maxAcceleration": 36.0,
    "maxDeceleration": 36.0,
    "fieldMap": "biobuzz.webp",
    "robotImage": "/robot.png",
    "showGhostPaths": false,
    "showOnionLayers": false,
    "onionLayerSpacing": 3,
    "onionColor": "#dc2626",
    "onionNextPointOnly": false,
    "showHeadingArrow": false,
    "showCurrentTValue": false,
    "leftPanelWidth": 262,
    "rightPanelWidth": 714,
    "headingArrowLength": 50,
    "headingArrowColor": "#ffffff",
    "headingArrowThickness": 2,
    "pathOpacity": 1,
    "leftPanelMinWidth": 0,
    "rightPanelMinWidth": 0,
    "penToolMaxPaths": 8,
    "curveThroughMaxPoints": 4,
    "experimentalFeatures": {
      "optimize": false,
      "curveThrough": false
    }
  },
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "exportName": "partner-leave-park",
    "registry": {
      "actions": [],
      "conditions": [],
      "typicalS": {},
      "events": []
    },
    "points": {
      "START": [
        24,
        132.25,
        270
      ],
      "SLIDE_P": [
        10.5,
        131,
        270
      ],
      "PARK_P": [
        10.5,
        111,
        270
      ],
      "PARK_P2": [
        10.5,
        110,
        270
      ]
    },
    "pathEnds": {
      "to-slide-p-1": "SLIDE_P",
      "to-park-p-2": "PARK_P",
      "to-park-p2-3": "PARK_P2"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "p-1",
        "kind": "path",
        "lineId": "to-slide-p-1",
        "park": false
      },
      {
        "id": "p-2",
        "kind": "path",
        "lineId": "to-park-p-2",
        "park": false
      },
      {
        "id": "p-3",
        "kind": "path",
        "lineId": "to-park-p2-3",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}