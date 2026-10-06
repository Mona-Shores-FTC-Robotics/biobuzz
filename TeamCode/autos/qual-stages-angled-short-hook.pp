{
  "startPoint": {
    "x": 59.0,
    "y": 6.75,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-s-fire-1",
      "color": "#3cc8e4",
      "name": "START to S_FIRE",
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
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-s-hook-2",
      "color": "#3cc8e4",
      "name": "S_FIRE to S_HOOK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 63.349999999999994,
        "y": 29.85
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-turn-3",
      "color": "#3cc8e4",
      "name": "S_HOOK to N_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
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
      "id": "to-n-low-4",
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
      "id": "to-row-s-5",
      "color": "#3cc8e4",
      "name": "N_LOW to ROW_S",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 49.34,
        "y": 112.53
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 137.3
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 137.3
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-row-n-6",
      "color": "#3cc8e4",
      "name": "ROW_S to ROW_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 46.33,
        "y": 115.31
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 137.3
      }
    },
    {
      "id": "to-n-low-7",
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
                "degrees": 137.3
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 137.3,
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
      "id": "to-far-flower-turn-8",
      "color": "#3cc8e4",
      "name": "N_LOW to FAR_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 122.04
      },
      "controlPoints": [
        {
          "x": 57.5,
          "y": 122.04
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
      "id": "to-far-flower-9",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_TURN to FAR_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 130.34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-fire-10",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to N_FIRE",
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
      "id": "to-n-hook-11",
      "color": "#3cc8e4",
      "name": "N_FIRE to N_HOOK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 63.349999999999994,
        "y": 111.65
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-fire-12",
      "color": "#3cc8e4",
      "name": "N_HOOK to S_FIRE",
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
      "id": "to-garden-in-13",
      "color": "#3cc8e4",
      "name": "S_FIRE to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 19.25
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.6,
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
      "id": "to-garden-14",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 8.25
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-fire-15",
      "color": "#3cc8e4",
      "name": "GARDEN to S_FIRE",
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
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.3,
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
        "y": 88.75
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
    },
    {
      "id": "to-n-hook-17",
      "color": "#3cc8e4",
      "name": "N_LOW to N_HOOK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 63.349999999999994,
        "y": 111.65
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-fire-18",
      "color": "#3cc8e4",
      "name": "N_HOOK to S_FIRE",
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
      "id": "to-garden-in-19",
      "color": "#3cc8e4",
      "name": "S_FIRE to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 19.25
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.6,
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
      "id": "to-garden-20",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 8.25
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-fire-21",
      "color": "#3cc8e4",
      "name": "GARDEN to S_FIRE",
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
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.3,
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
      "id": "to-park-22",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 88.75
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
      "lineId": "to-s-fire-1"
    },
    {
      "kind": "path",
      "lineId": "to-s-hook-2"
    },
    {
      "kind": "path",
      "lineId": "to-n-turn-3"
    },
    {
      "kind": "path",
      "lineId": "to-n-low-4"
    },
    {
      "kind": "path",
      "lineId": "to-row-s-5"
    },
    {
      "kind": "path",
      "lineId": "to-row-n-6"
    },
    {
      "kind": "path",
      "lineId": "to-n-low-7"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-8"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-9"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-10"
    },
    {
      "kind": "path",
      "lineId": "to-n-hook-11"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-12"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-13"
    },
    {
      "kind": "path",
      "lineId": "to-garden-14"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-15"
    },
    {
      "kind": "path",
      "lineId": "to-park-16"
    },
    {
      "kind": "path",
      "lineId": "to-n-hook-17"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-18"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-19"
    },
    {
      "kind": "path",
      "lineId": "to-garden-20"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-21"
    },
    {
      "kind": "path",
      "lineId": "to-park-22"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 12.5,
    "rHeight": 12.5,
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
    "exportName": "qual-stages-angled-short-hook",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll"
      ],
      "conditions": [
        "Empty",
        "LeftCellUp",
        "IntakeFull",
        "Tip",
        "RightCellUp"
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
        59.0,
        6.75,
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
        57.5,
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
        19.25,
        270
      ],
      "GARDEN": [
        8.5,
        8.25,
        270
      ],
      "PARK": [
        13.0,
        88.75,
        90
      ],
      "FAR_FLOWER": [
        47.36,
        130.34,
        90
      ],
      "FAR_FLOWER_IN": [
        47.36,
        124.54,
        90
      ],
      "FAR_FLOWER_TURN": [
        47.36,
        122.04,
        90
      ],
      "WALL_FLOWER": [
        11.16,
        47.36,
        180
      ],
      "WALL_FLOWER_IN": [
        16.96,
        47.36,
        180
      ],
      "WALL_FLOWER_TURN": [
        19.46,
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
        114,
        270
      ],
      "S_HOOK": [
        63.349999999999994,
        29.85,
        90
      ],
      "ROW_S": [
        49.34,
        112.53,
        137.3
      ],
      "ROW_N": [
        46.33,
        115.31,
        137.3
      ],
      "LANE_N": [
        36,
        100,
        90
      ],
      "N_HOOK": [
        63.349999999999994,
        111.65,
        270
      ]
    },
    "pathEnds": {
      "to-s-fire-1": "S_FIRE",
      "to-s-hook-2": "S_HOOK",
      "to-n-turn-3": "N_TURN",
      "to-n-low-4": "N_LOW",
      "to-row-s-5": "ROW_S",
      "to-row-n-6": "ROW_N",
      "to-n-low-7": "N_LOW",
      "to-far-flower-turn-8": "FAR_FLOWER_TURN",
      "to-far-flower-9": "FAR_FLOWER",
      "to-n-fire-10": "N_FIRE",
      "to-n-hook-11": "N_HOOK",
      "to-s-fire-12": "S_FIRE",
      "to-garden-in-13": "GARDEN_IN",
      "to-garden-14": "GARDEN",
      "to-s-fire-15": "S_FIRE",
      "to-park-16": "PARK",
      "to-n-hook-17": "N_HOOK",
      "to-s-fire-18": "S_FIRE",
      "to-garden-in-19": "GARDEN_IN",
      "to-garden-20": "GARDEN",
      "to-s-fire-21": "S_FIRE",
      "to-park-22": "PARK"
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
        "lineId": "to-s-fire-1",
        "park": false
      },
      {
        "id": "w-3",
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
        "lineId": "to-s-hook-2",
        "park": false
      },
      {
        "id": "w-5",
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
        "id": "w-6",
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
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-7",
        "kind": "path",
        "lineId": "to-n-turn-3",
        "park": false
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-n-low-4",
        "park": false
      },
      {
        "id": "w-9",
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
        "id": "p-10",
        "kind": "path",
        "lineId": "to-row-s-5",
        "park": false
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-row-n-6",
        "park": false
      },
      {
        "id": "w-12",
        "kind": "firstOf",
        "label": "The staged row",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1600,
            "cards": []
          }
        ]
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-n-low-7",
        "park": false
      },
      {
        "id": "w-14",
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
        "id": "w-42",
        "kind": "firstOf",
        "label": "TIP 2?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
              {
                "id": "p-31",
                "kind": "path",
                "lineId": "to-n-hook-17",
                "park": false
              },
              {
                "id": "w-32",
                "kind": "firstOf",
                "label": "TIP 2 settles",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
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
                "id": "w-33",
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
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-34",
                "kind": "path",
                "lineId": "to-s-fire-18",
                "park": false
              },
              {
                "id": "w-35",
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
                "id": "p-36",
                "kind": "path",
                "lineId": "to-garden-in-19",
                "park": false
              },
              {
                "id": "p-37",
                "kind": "path",
                "lineId": "to-garden-20",
                "park": false
              },
              {
                "id": "w-38",
                "kind": "firstOf",
                "label": "The GARDEN",
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
                "id": "p-39",
                "kind": "path",
                "lineId": "to-s-fire-21",
                "park": false
              },
              {
                "id": "w-40",
                "kind": "firstOf",
                "label": "Fire the GARDEN",
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
                "id": "p-41",
                "kind": "path",
                "lineId": "to-park-22",
                "park": true
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 600,
            "cards": [
              {
                "id": "p-15",
                "kind": "path",
                "lineId": "to-far-flower-turn-8",
                "park": false
              },
              {
                "id": "p-16",
                "kind": "path",
                "lineId": "to-far-flower-9",
                "park": false
              },
              {
                "id": "w-17",
                "kind": "firstOf",
                "label": "The far FLOWER",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2300,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-18",
                "kind": "path",
                "lineId": "to-n-fire-10",
                "park": false
              },
              {
                "id": "w-19",
                "kind": "firstOf",
                "label": "Fire the far FLOWER (TIP 2)",
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
                "id": "p-20",
                "kind": "path",
                "lineId": "to-n-hook-11",
                "park": false
              },
              {
                "id": "w-21",
                "kind": "firstOf",
                "label": "TIP 2 settles (B)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
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
                "id": "w-22",
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
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-23",
                "kind": "path",
                "lineId": "to-s-fire-12",
                "park": false
              },
              {
                "id": "w-24",
                "kind": "firstOf",
                "label": "Fire TIP 2's spill (B)",
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
                "id": "p-25",
                "kind": "path",
                "lineId": "to-garden-in-13",
                "park": false
              },
              {
                "id": "p-26",
                "kind": "path",
                "lineId": "to-garden-14",
                "park": false
              },
              {
                "id": "w-27",
                "kind": "firstOf",
                "label": "The GARDEN (B)",
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
                "id": "p-28",
                "kind": "path",
                "lineId": "to-s-fire-15",
                "park": false
              },
              {
                "id": "w-29",
                "kind": "firstOf",
                "label": "Fire the GARDEN (B)",
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
                "id": "p-30",
                "kind": "path",
                "lineId": "to-park-16",
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