{
  "startPoint": {
    "x": 59,
    "y": 133.69,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-far-flower-turn-1",
      "color": "#3cc8e4",
      "name": "START to FAR_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 118.34
      },
      "controlPoints": [
        {
          "x": 59,
          "y": 118.34
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-far-flower-2",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_TURN to FAR_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 126.64
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-fire-3",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to N_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 119
      },
      "controlPoints": [
        {
          "x": 47.36,
          "y": 116.64
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-turn-4",
      "color": "#3cc8e4",
      "name": "N_FIRE to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.16,
        "y": 47.36
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 112
        },
        {
          "x": 30,
          "y": 64
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.15,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.15,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-5",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.86,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-fire3-6",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to FIRE3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 26
      },
      "controlPoints": [
        {
          "x": 24.86,
          "y": 47.36
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-in-7",
      "color": "#3cc8e4",
      "name": "FIRE3 to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 20.56
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-8",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 10.96
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-fire-g-9",
      "color": "#3cc8e4",
      "name": "GARDEN to FIRE_G",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 26
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-10",
      "color": "#3cc8e4",
      "name": "FIRE_G to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 95
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
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
      "lineId": "to-far-flower-turn-1"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-2"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-3"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-4"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-5"
    },
    {
      "kind": "path",
      "lineId": "to-fire3-6"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-7"
    },
    {
      "kind": "path",
      "lineId": "to-garden-8"
    },
    {
      "kind": "path",
      "lineId": "to-fire-g-9"
    },
    {
      "kind": "path",
      "lineId": "to-park-10"
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
    "maxVelocity": 50,
    "maxAcceleration": 45.0,
    "maxDeceleration": 45.0,
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
    "exportName": "qual-shoots-right-v-fixed-west45",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll"
      ],
      "conditions": [
        "LeftCellUp",
        "Empty",
        "IntakeFull",
        "Tip"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "LaunchAll": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        133.69,
        270
      ],
      "S_FIRE": [
        55,
        24,
        270
      ],
      "N_FIRE": [
        57.5,
        119,
        270
      ],
      "PARK": [
        10.5,
        95,
        90
      ],
      "FIRE3": [
        45,
        26,
        90
      ],
      "FAR_FLOWER": [
        47.36,
        126.64,
        90
      ],
      "FAR_FLOWER_IN": [
        47.36,
        120.84,
        90
      ],
      "FAR_FLOWER_TURN": [
        47.36,
        118.34,
        90
      ],
      "WALL_FLOWER": [
        14.86,
        47.36,
        180
      ],
      "WALL_FLOWER_IN": [
        20.66,
        47.36,
        180
      ],
      "WALL_FLOWER_TURN": [
        23.16,
        47.36,
        180
      ],
      "GARDEN_IN": [
        9.5,
        20.56,
        270
      ],
      "GARDEN": [
        9.5,
        10.96,
        270
      ],
      "FIRE_G": [
        45,
        26,
        90
      ]
    },
    "pathEnds": {
      "to-far-flower-turn-1": "FAR_FLOWER_TURN",
      "to-far-flower-2": "FAR_FLOWER",
      "to-n-fire-3": "N_FIRE",
      "to-wall-flower-turn-4": "WALL_FLOWER_TURN",
      "to-wall-flower-5": "WALL_FLOWER",
      "to-fire3-6": "FIRE3",
      "to-garden-in-7": "GARDEN_IN",
      "to-garden-8": "GARDEN",
      "to-fire-g-9": "FIRE_G",
      "to-park-10": "PARK"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "p-2",
        "kind": "path",
        "lineId": "to-far-flower-turn-1",
        "park": false
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "TIP 1 (the partner's preloads)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 6000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "Fire our preloads at the left CELL",
        "rows": [
          {
            "when": [
              "Empty"
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
        "id": "p-5",
        "kind": "path",
        "lineId": "to-far-flower-2",
        "park": false
      },
      {
        "id": "w-6",
        "kind": "firstOf",
        "label": "The far FLOWER's 4",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-7",
        "kind": "path",
        "lineId": "to-n-fire-3",
        "park": false
      },
      {
        "id": "w-8",
        "kind": "firstOf",
        "label": "Fire the far FLOWER's 4 (TIP 2)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-9",
        "kind": "path",
        "lineId": "to-wall-flower-turn-4",
        "park": false
      },
      {
        "id": "p-10",
        "kind": "path",
        "lineId": "to-wall-flower-5",
        "park": false
      },
      {
        "id": "w-11",
        "kind": "firstOf",
        "label": "The wall FLOWER's 4",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-fire3-6",
        "park": false
      },
      {
        "id": "w-13",
        "kind": "firstOf",
        "label": "Fire the wall FLOWER's 4 at the right CELL",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-14",
        "kind": "path",
        "lineId": "to-garden-in-7",
        "park": false
      },
      {
        "id": "p-15",
        "kind": "path",
        "lineId": "to-garden-8",
        "park": false
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "The GARDEN's 4",
        "rows": [
          {
            "when": [
              "IntakeFull"
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
        "id": "p-17",
        "kind": "path",
        "lineId": "to-fire-g-9",
        "park": false
      },
      {
        "id": "w-18",
        "kind": "firstOf",
        "label": "Fire the GARDEN's 4 (TIP 3)",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-19",
        "kind": "path",
        "lineId": "to-park-10",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}