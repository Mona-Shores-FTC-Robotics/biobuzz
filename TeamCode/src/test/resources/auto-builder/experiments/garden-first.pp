{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-garden-in-1",
      "color": "#3cc8e4",
      "name": "START to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 22
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 22
        }
      ],
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
      "id": "to-garden-2",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
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
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-left-shot-3",
      "color": "#3cc8e4",
      "name": "GARDEN to LEFT_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 116
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 30
        },
        {
          "x": 24,
          "y": 92
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
              "endProgress": 0.95,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 301
              }
            },
            {
              "startProgress": 0.95,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 301
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-shot-4",
      "color": "#3cc8e4",
      "name": "LEFT_SHOT to SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 20
      },
      "controlPoints": [
        {
          "x": 34,
          "y": 92
        },
        {
          "x": 12,
          "y": 60
        },
        {
          "x": 22,
          "y": 18
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 301,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.8,
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
      "id": "to-sweep-1-5",
      "color": "#3cc8e4",
      "name": "SHOT to SWEEP_1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-sweep-2-6",
      "color": "#3cc8e4",
      "name": "SWEEP_1 to SWEEP_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 38
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-shot-7",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 20
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-sweep-1-8",
      "color": "#3cc8e4",
      "name": "SHOT to SWEEP_1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-sweep-2-9",
      "color": "#3cc8e4",
      "name": "SWEEP_1 to SWEEP_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 38
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-shot-10",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 20
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-11",
      "color": "#3cc8e4",
      "name": "SHOT to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 89.5
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 30
        },
        {
          "x": 22,
          "y": 80
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
      "lineId": "to-garden-in-1"
    },
    {
      "kind": "path",
      "lineId": "to-garden-2"
    },
    {
      "kind": "path",
      "lineId": "to-left-shot-3"
    },
    {
      "kind": "path",
      "lineId": "to-shot-4"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-1-5"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-2-6"
    },
    {
      "kind": "path",
      "lineId": "to-shot-7"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-1-8"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-2-9"
    },
    {
      "kind": "path",
      "lineId": "to-shot-10"
    },
    {
      "kind": "path",
      "lineId": "to-park-11"
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
    "exportName": "garden-first",
    "registry": {
      "actions": [
        "LaunchAll"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp"
      ],
      "typicalS": {
        "LaunchAll": 2.0
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "GARDEN_IN": [
        8.5,
        22,
        270
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "LEFT_SHOT": [
        40,
        116,
        301
      ],
      "SHOT": [
        57.5,
        20,
        90
      ],
      "PARK": [
        15,
        89.5,
        90
      ],
      "SWEEP_1": [
        57.5,
        30,
        90
      ],
      "SWEEP_2": [
        57.5,
        38,
        90
      ]
    },
    "pathEnds": {
      "to-garden-in-1": "GARDEN_IN",
      "to-garden-2": "GARDEN",
      "to-left-shot-3": "LEFT_SHOT",
      "to-shot-4": "SHOT",
      "to-sweep-1-5": "SWEEP_1",
      "to-sweep-2-6": "SWEEP_2",
      "to-shot-7": "SHOT",
      "to-sweep-1-8": "SWEEP_1",
      "to-sweep-2-9": "SWEEP_2",
      "to-shot-10": "SHOT",
      "to-park-11": "PARK"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "w-1",
        "kind": "firstOf",
        "label": "Fire the preloads (TIP 1)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 4500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-2",
        "kind": "path",
        "lineId": "to-garden-in-1",
        "park": false
      },
      {
        "id": "p-3",
        "kind": "path",
        "lineId": "to-garden-2",
        "park": false
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "Collect in the GARDEN",
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
        "id": "p-5",
        "kind": "path",
        "lineId": "to-left-shot-3",
        "park": false
      },
      {
        "id": "w-6",
        "kind": "firstOf",
        "label": "Fire at the left CELL (TIP 2)",
        "rows": [
          {
            "when": [
              "Empty"
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
        "id": "p-7",
        "kind": "path",
        "lineId": "to-shot-4",
        "park": false
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-sweep-1-5",
        "park": false
      },
      {
        "id": "w-9",
        "kind": "firstOf",
        "label": "Sweep up the TIP 1 spill (1)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-10",
        "kind": "path",
        "lineId": "to-sweep-2-6",
        "park": false
      },
      {
        "id": "w-11",
        "kind": "firstOf",
        "label": "Sweep up the TIP 1 spill (2)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 900,
            "cards": []
          }
        ]
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-shot-7",
        "park": false
      },
      {
        "id": "w-13",
        "kind": "firstOf",
        "label": "Fire the spill (TIP 3)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 1800,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "TIP 3 yet?",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [],
            "label": "Yes"
          },
          {
            "afterMs": 1500,
            "cards": [
              {
                "id": "p-14",
                "kind": "path",
                "lineId": "to-sweep-1-8",
                "park": false
              },
              {
                "id": "w-15",
                "kind": "firstOf",
                "label": "Sweep up the rest (1)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 500,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-16",
                "kind": "path",
                "lineId": "to-sweep-2-9",
                "park": false
              },
              {
                "id": "w-17",
                "kind": "firstOf",
                "label": "Sweep up the rest (2)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 900,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-18",
                "kind": "path",
                "lineId": "to-shot-10",
                "park": false
              },
              {
                "id": "w-19",
                "kind": "firstOf",
                "label": "Fire the rest (TIP 3)",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2500,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              }
            ],
            "label": "No: the rest of the spill"
          }
        ]
      },
      {
        "id": "p-21",
        "kind": "path",
        "lineId": "to-park-11",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}