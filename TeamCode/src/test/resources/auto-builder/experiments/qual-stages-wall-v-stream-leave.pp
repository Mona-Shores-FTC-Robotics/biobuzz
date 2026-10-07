{
  "startPoint": {
    "x": 59.0,
    "y": 8.06,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-s-catch-1",
      "color": "#3cc8e4",
      "name": "START to S_CATCH",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 28
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-n-turn-2",
      "color": "#3cc8e4",
      "name": "S_CATCH to N_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55.5,
        "y": 104
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.95,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.95,
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
      "id": "to-n-low-3",
      "color": "#3cc8e4",
      "name": "N_TURN to N_LOW",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-row-s-4",
      "color": "#3cc8e4",
      "name": "N_LOW to ROW_S",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35.1,
        "y": 117.35
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.7,
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
      "id": "to-row-1-5",
      "color": "#3cc8e4",
      "name": "ROW_S to ROW_1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35.1,
        "y": 119.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-2-6",
      "color": "#3cc8e4",
      "name": "ROW_1 to ROW_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35.1,
        "y": 122.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-3-7",
      "color": "#3cc8e4",
      "name": "ROW_2 to ROW_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35.1,
        "y": 126.15
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-4-8",
      "color": "#3cc8e4",
      "name": "ROW_3 to ROW_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35.1,
        "y": 128.95
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-n-9",
      "color": "#3cc8e4",
      "name": "ROW_4 to ROW_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35.1,
        "y": 130.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-low-10",
      "color": "#3cc8e4",
      "name": "ROW_N to N_LOW",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 114
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
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.9,
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
      "id": "to-far-flower-turn-11",
      "color": "#3cc8e4",
      "name": "N_LOW to FAR_FLOWER_TURN",
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
          "x": 57.5,
          "y": 118.34
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-far-flower-12",
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
      "id": "to-n-fire-13",
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
      "id": "to-park-14",
      "color": "#3cc8e4",
      "name": "N_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 87.44
      },
      "controlPoints": [
        {
          "x": 57.5,
          "y": -5
        },
        {
          "x": 57.5,
          "y": -10
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.45,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.45,
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
      "id": "to-s-fire-15",
      "color": "#3cc8e4",
      "name": "N_LOW to S_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.86,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.86,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-16",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 87.44
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 24
        },
        {
          "x": 24,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
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
      "lineId": "to-s-catch-1"
    },
    {
      "kind": "path",
      "lineId": "to-n-turn-2"
    },
    {
      "kind": "path",
      "lineId": "to-n-low-3"
    },
    {
      "kind": "path",
      "lineId": "to-row-s-4"
    },
    {
      "kind": "path",
      "lineId": "to-row-1-5"
    },
    {
      "kind": "path",
      "lineId": "to-row-2-6"
    },
    {
      "kind": "path",
      "lineId": "to-row-3-7"
    },
    {
      "kind": "path",
      "lineId": "to-row-4-8"
    },
    {
      "kind": "path",
      "lineId": "to-row-n-9"
    },
    {
      "kind": "path",
      "lineId": "to-n-low-10"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-11"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-12"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-13"
    },
    {
      "kind": "path",
      "lineId": "to-park-14"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-15"
    },
    {
      "kind": "path",
      "lineId": "to-park-16"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 15.12,
    "rHeight": 15.12,
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
    "exportName": "qual-stages-wall-v-stream-leave",
    "registry": {
      "actions": [
        "LaunchAll",
        "StreamOn",
        "StreamOff"
      ],
      "conditions": [
        "Empty",
        "LeftCellUp",
        "IntakeFull",
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2.0,
        "StreamOn": 1.0,
        "StreamOff": 1.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59.0,
        8.06,
        90
      ],
      "S_SHOOT": [
        57.5,
        14,
        90
      ],
      "N_SHOOT": [
        57.5,
        127.5,
        270
      ],
      "N_LOW": [
        57.5,
        114,
        270
      ],
      "N_TURN": [
        55.5,
        104,
        270
      ],
      "S_CATCH": [
        57.5,
        28,
        90
      ],
      "N_CATCH": [
        57.5,
        113.5,
        270
      ],
      "S_BACK": [
        57.5,
        25,
        90
      ],
      "N_BACK": [
        57.5,
        116.5,
        270
      ],
      "GARDEN_IN": [
        8.5,
        20.56,
        270
      ],
      "GARDEN": [
        8.5,
        10.96,
        270
      ],
      "PARK": [
        13.0,
        87.44,
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
      "S_FIRE": [
        57.5,
        24,
        90
      ],
      "N_FIRE": [
        57.5,
        119,
        270
      ],
      "ROW_S": [
        35.1,
        117.35,
        90
      ],
      "ROW_1": [
        35.1,
        119.75,
        90
      ],
      "ROW_2": [
        35.1,
        122.75,
        90
      ],
      "ROW_3": [
        35.1,
        126.15,
        90
      ],
      "ROW_4": [
        35.1,
        128.95,
        90
      ],
      "ROW_N": [
        35.1,
        130.75,
        90
      ]
    },
    "pathEnds": {
      "to-s-catch-1": "S_CATCH",
      "to-n-turn-2": "N_TURN",
      "to-n-low-3": "N_LOW",
      "to-row-s-4": "ROW_S",
      "to-row-1-5": "ROW_1",
      "to-row-2-6": "ROW_2",
      "to-row-3-7": "ROW_3",
      "to-row-4-8": "ROW_4",
      "to-row-n-9": "ROW_N",
      "to-n-low-10": "N_LOW",
      "to-far-flower-turn-11": "FAR_FLOWER_TURN",
      "to-far-flower-12": "FAR_FLOWER",
      "to-n-fire-13": "N_FIRE",
      "to-park-14": "PARK",
      "to-s-fire-15": "S_FIRE",
      "to-park-16": "PARK"
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
            "afterMs": 4000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-4",
        "kind": "path",
        "lineId": "to-s-catch-1",
        "park": false
      },
      {
        "id": "w-2",
        "kind": "firstOf",
        "label": "TIP 1 settles",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 3500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "It lands",
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
        "id": "p-5",
        "kind": "path",
        "lineId": "to-n-turn-2",
        "park": false
      },
      {
        "id": "p-6",
        "kind": "path",
        "lineId": "to-n-low-3",
        "park": false
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "Fire the catch",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2200,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-row-s-4",
        "park": false
      },
      {
        "id": "p-9",
        "kind": "path",
        "lineId": "to-row-1-5",
        "park": false
      },
      {
        "id": "w-10",
        "kind": "firstOf",
        "label": "Row piece 1",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-row-2-6",
        "park": false
      },
      {
        "id": "w-12",
        "kind": "firstOf",
        "label": "Row piece 2",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-row-3-7",
        "park": false
      },
      {
        "id": "w-14",
        "kind": "firstOf",
        "label": "Row piece 3",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-15",
        "kind": "path",
        "lineId": "to-row-4-8",
        "park": false
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "Row piece 4",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-17",
        "kind": "path",
        "lineId": "to-row-n-9",
        "park": false
      },
      {
        "id": "w-18",
        "kind": "firstOf",
        "label": "Row piece 5",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-19",
        "kind": "path",
        "lineId": "to-n-low-10",
        "park": false
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "Fire the row",
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
        "id": "w-36",
        "kind": "firstOf",
        "label": "TIP 2?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
              {
                "id": "w-32",
                "kind": "firstOf",
                "label": "It lands",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1300,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-33",
                "kind": "path",
                "lineId": "to-s-fire-15",
                "park": false
              },
              {
                "id": "w-34",
                "kind": "firstOf",
                "label": "Fire TIP 2's spill",
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
                "id": "p-35",
                "kind": "path",
                "lineId": "to-park-16",
                "park": true
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 600,
            "cards": [
              {
                "id": "p-21",
                "kind": "path",
                "lineId": "to-far-flower-turn-11",
                "park": false
              },
              {
                "id": "p-22",
                "kind": "path",
                "lineId": "to-far-flower-12",
                "park": false
              },
              {
                "id": "w-24",
                "kind": "firstOf",
                "label": "Seated at the FLOWER",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 400,
                    "cards": []
                  }
                ]
              },
              {
                "id": "a-25",
                "kind": "action",
                "name": "StreamOn"
              },
              {
                "id": "w-26",
                "kind": "firstOf",
                "label": "The far FLOWER, fired as it comes out (TIP 2)",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1300,
                    "cards": []
                  }
                ]
              },
              {
                "id": "a-27",
                "kind": "action",
                "name": "StreamOff"
              },
              {
                "id": "p-28",
                "kind": "path",
                "lineId": "to-n-fire-13",
                "park": false
              },
              {
                "id": "a-29",
                "kind": "action",
                "name": "StreamOff"
              },
              {
                "id": "w-30",
                "kind": "firstOf",
                "label": "It lands (B)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1300,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-31",
                "kind": "path",
                "lineId": "to-park-14",
                "park": true
              }
            ],
            "label": "No: the far FLOWER"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}