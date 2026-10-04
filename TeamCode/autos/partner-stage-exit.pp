{
  "startPoint": {
    "x": 59,
    "y": 132.25,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-stage-1",
      "color": "#3cc8e4",
      "name": "START to STAGE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 120.0
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-lane-2",
      "color": "#3cc8e4",
      "name": "STAGE to LANE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 58,
        "y": 127.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-park-p-3",
      "color": "#3cc8e4",
      "name": "LANE to PARK_P",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 111
      },
      "controlPoints": [
        {
          "x": 12,
          "y": 127.5
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
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
      "lineId": "to-stage-1"
    },
    {
      "kind": "path",
      "lineId": "to-lane-2"
    },
    {
      "kind": "path",
      "lineId": "to-park-p-3"
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
    "exportName": "partner-stage-exit",
    "registry": {
      "actions": [
        "SetDown"
      ],
      "conditions": [
        "Empty"
      ],
      "typicalS": {
        "SetDown": 1.0
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        132.25,
        270
      ],
      "STAGE": [
        59,
        120.0,
        270
      ],
      "LANE": [
        58,
        127.5,
        270
      ],
      "PARK_P": [
        10.5,
        111,
        270
      ]
    },
    "pathEnds": {
      "to-stage-1": "STAGE",
      "to-lane-2": "LANE",
      "to-park-p-3": "PARK_P"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "p-1",
        "kind": "path",
        "lineId": "to-stage-1",
        "park": false
      },
      {
        "id": "w-2",
        "kind": "firstOf",
        "label": "Set the preloads down",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ],
        "alongside": "SetDown"
      },
      {
        "id": "p-3",
        "kind": "path",
        "lineId": "to-lane-2",
        "park": false
      },
      {
        "id": "p-4",
        "kind": "path",
        "lineId": "to-park-p-3",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}