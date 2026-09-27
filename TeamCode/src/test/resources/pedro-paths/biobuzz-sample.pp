{
  "startPoint": {
    "x": 56,
    "y": 8,
    "headingDeg": 90,
    "name": "Start"
  },
  "lines": [
    {
      "color": "#ffc516",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "id": "leave-start",
      "name": "Leave start",
      "endPoint": {
        "x": 56,
        "y": 36
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 135
      }
    },
    {
      "color": "#ffc516",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "id": "arc-to-score",
      "name": "Arc to score",
      "endPoint": {
        "x": 40,
        "y": 70
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 44
        }
      ],
      "heading": {
        "type": "tangential",
        "reverse": false
      }
    },
    {
      "color": "#ffc516",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "id": "face-the-hive",
      "name": "Face the HIVE",
      "endPoint": {
        "x": 36,
        "y": 100
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 120
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "facing-point",
              "parameters": {
                "point": {
                  "x": 70.75,
                  "y": 70.75
                }
              }
            }
          ]
        }
      }
    },
    {
      "color": "#ffc516",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "id": "sweep",
      "name": "Sweep",
      "heading": {
        "type": "linear",
        "startDeg": 120,
        "endDeg": 180
      },
      "segments": [
        {
          "color": "#ffc516",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "id": "sweep-a",
          "name": "Sweep A",
          "endPoint": {
            "x": 20,
            "y": 120
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 180
          }
        },
        {
          "color": "#ffc516",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "id": "sweep-b",
          "name": "Sweep B",
          "endPoint": {
            "x": 20,
            "y": 132
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 180
          }
        }
      ]
    },
    {
      "color": "#ffc516",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "id": "through-pickups",
      "name": "Through pickups",
      "endPoint": {
        "x": 60,
        "y": 120
      },
      "controlPoints": [],
      "throughPoints": [
        {
          "x": 34,
          "y": 124
        },
        {
          "x": 48,
          "y": 112
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "color": "#ffc516",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 500,
      "waitBeforeName": "",
      "waitAfterName": "Settle",
      "kind": "atomic",
      "id": "return",
      "name": "Return",
      "endPoint": {
        "x": 40,
        "y": 70
      },
      "controlPoints": [
        {
          "x": 60,
          "y": 90
        }
      ],
      "heading": {
        "type": "tangential",
        "reverse": true
      }
    }
  ],
  "shapes": [],
  "sequence": [
    {
      "kind": "path",
      "lineId": "leave-start"
    },
    {
      "kind": "path",
      "lineId": "arc-to-score"
    },
    {
      "kind": "wait",
      "id": "score-wait",
      "name": "Score",
      "durationMs": 750
    },
    {
      "kind": "path",
      "lineId": "face-the-hive"
    },
    {
      "kind": "path",
      "lineId": "sweep-a"
    },
    {
      "kind": "path",
      "lineId": "sweep-b"
    },
    {
      "kind": "path",
      "lineId": "through-pickups"
    },
    {
      "kind": "path",
      "lineId": "return"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 16,
    "rHeight": 16,
    "safetyMargin": 1,
    "maxVelocity": 40,
    "maxAcceleration": 30,
    "maxDeceleration": 30,
    "fieldMap": "biobuzz.webp",
    "robotImage": "/robot.png",
    "showGhostPaths": false,
    "showOnionLayers": false,
    "onionLayerSpacing": 3,
    "onionColor": "#dc2626",
    "onionNextPointOnly": false,
    "showHeadingArrow": false,
    "showCurrentTValue": false,
    "leftPanelWidth": 370,
    "rightPanelWidth": 620,
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
  "version": "1.5.0",
  "timestamp": "2026-09-27T00:00:00.000Z"
}
