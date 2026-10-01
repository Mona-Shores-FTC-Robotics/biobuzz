{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-wall-flower-1",
      "color": "#3cc8e4",
      "name": "START to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 11,
        "y": 47.4
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      }
    },
    {
      "id": "to-north-shot-2",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to NORTH_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 116
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 301
      }
    },
    {
      "id": "to-far-flower-3",
      "color": "#3cc8e4",
      "name": "NORTH_SHOT to FAR_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.4,
        "y": 130.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 301,
        "endDeg": 90
      }
    },
    {
      "id": "to-north-shot-4",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to NORTH_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 116
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 301
      }
    },
    {
      "id": "to-garden-5",
      "color": "#3cc8e4",
      "name": "NORTH_SHOT to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 11
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 301,
        "endDeg": 270
      }
    },
    {
      "id": "to-south-shot-6",
      "color": "#3cc8e4",
      "name": "GARDEN to SOUTH_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 36,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 49
      }
    },
    {
      "id": "to-park-7",
      "color": "#3cc8e4",
      "name": "SOUTH_SHOT to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 99
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 49,
        "endDeg": 90
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
      "lineId": "to-wall-flower-1"
    },
    {
      "kind": "path",
      "lineId": "to-north-shot-2"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-3"
    },
    {
      "kind": "path",
      "lineId": "to-north-shot-4"
    },
    {
      "kind": "path",
      "lineId": "to-garden-5"
    },
    {
      "kind": "path",
      "lineId": "to-south-shot-6"
    },
    {
      "kind": "path",
      "lineId": "to-park-7"
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
    "maxVelocity": 60,
    "maxAcceleration": 55,
    "maxDeceleration": 55,
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
    "exportName": "solo-two-tip",
    "registry": {
      "actions": [
        "LaunchOne",
        "LaunchAll"
      ],
      "conditions": [
        "Tip",
        "IntakeFull"
      ],
      "typicalS": {
        "LaunchOne": 0.5,
        "LaunchAll": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "WALL_FLOWER": [
        11,
        47.4,
        180
      ],
      "NORTH_SHOT": [
        40,
        116,
        301
      ],
      "FAR_FLOWER": [
        47.4,
        130.5,
        90
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "SOUTH_SHOT": [
        36,
        30,
        49
      ],
      "PARK": [
        15,
        99,
        90
      ]
    },
    "pathEnds": {
      "to-wall-flower-1": "WALL_FLOWER",
      "to-north-shot-2": "NORTH_SHOT",
      "to-far-flower-3": "FAR_FLOWER",
      "to-north-shot-4": "NORTH_SHOT",
      "to-garden-5": "GARDEN",
      "to-south-shot-6": "SOUTH_SHOT",
      "to-park-7": "PARK"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-2",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-3",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "Tip 1",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-5",
        "kind": "path",
        "lineId": "to-wall-flower-1",
        "park": false
      },
      {
        "id": "w-6",
        "kind": "firstOf",
        "label": "Collect at WALL_FLOWER",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-7",
        "kind": "path",
        "lineId": "to-north-shot-2",
        "park": false
      },
      {
        "id": "a-8",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "p-9",
        "kind": "path",
        "lineId": "to-far-flower-3",
        "park": false
      },
      {
        "id": "w-10",
        "kind": "firstOf",
        "label": "Collect at FAR_FLOWER",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-north-shot-4",
        "park": false
      },
      {
        "id": "w-12",
        "kind": "firstOf",
        "label": "Tip 2",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-garden-5",
        "park": false
      },
      {
        "id": "w-14",
        "kind": "firstOf",
        "label": "Collect in GARDEN",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-15",
        "kind": "path",
        "lineId": "to-south-shot-6",
        "park": false
      },
      {
        "id": "a-16",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "p-17",
        "kind": "path",
        "lineId": "to-park-7",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}