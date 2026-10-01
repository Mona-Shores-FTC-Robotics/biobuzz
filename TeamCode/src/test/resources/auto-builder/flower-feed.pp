{
  "startPoint": {
    "x": 59,
    "y": 132.25,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-feed-turn-1",
      "color": "#3cc8e4",
      "name": "START to FEED_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 119.29
      },
      "controlPoints": [
        {
          "x": 59,
          "y": 119.29
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
      "id": "to-feed-2",
      "color": "#3cc8e4",
      "name": "FEED_TURN to FEED",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 127.59
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-catch-3",
      "color": "#3cc8e4",
      "name": "FEED to CATCH",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 121
      },
      "controlPoints": [
        {
          "x": 47.36,
          "y": 116
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
      "id": "to-l-exit-back-4",
      "color": "#3cc8e4",
      "name": "CATCH to L_EXIT_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 56.5,
        "y": 103
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-exit-back-5",
      "color": "#3cc8e4",
      "name": "L_EXIT_BACK to R_EXIT_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-home-6",
      "color": "#3cc8e4",
      "name": "R_EXIT_BACK to R_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.5,
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
      "name": "R_HOME to GARDEN_IN",
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
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.65,
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
      "id": "to-r-back-9",
      "color": "#3cc8e4",
      "name": "GARDEN to R_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 11
      },
      "controlPoints": [
        {
          "x": 20,
          "y": 18
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.65,
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
      "id": "to-r-back-10",
      "color": "#3cc8e4",
      "name": "R_HOME to R_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 11
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
      "name": "R_HOME to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 90
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 24
        },
        {
          "x": 24,
          "y": 85
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
      "lineId": "to-feed-turn-1"
    },
    {
      "kind": "path",
      "lineId": "to-feed-2"
    },
    {
      "kind": "path",
      "lineId": "to-catch-3"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-back-4"
    },
    {
      "kind": "path",
      "lineId": "to-r-exit-back-5"
    },
    {
      "kind": "path",
      "lineId": "to-r-home-6"
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
      "lineId": "to-r-back-9"
    },
    {
      "kind": "path",
      "lineId": "to-r-back-10"
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
    "exportName": "flower-feed",
    "registry": {
      "actions": [
        "StreamOn",
        "StreamOff",
        "LaunchAll",
        "CollectSeen",
        "SpinUp"
      ],
      "conditions": [
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "Empty"
      ],
      "typicalS": {
        "StreamOn": 1.0,
        "StreamOff": 1.0,
        "LaunchAll": 2.0,
        "CollectSeen": 2.0,
        "SpinUp": 0.1
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        132.25,
        270
      ],
      "L_HOME": [
        59,
        131.75,
        270
      ],
      "R_HOME": [
        57.5,
        10,
        90
      ],
      "L_EXIT": [
        56.5,
        103,
        90
      ],
      "L_EXIT_BACK": [
        56.5,
        103,
        270
      ],
      "R_EXIT_BACK": [
        57.5,
        34,
        270
      ],
      "R_EXIT": [
        57.5,
        34,
        90
      ],
      "R_BACK": [
        59,
        11,
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
      "PARK": [
        15,
        90,
        90
      ],
      "FEED": [
        47.36,
        127.59,
        90
      ],
      "FEED_IN": [
        47.36,
        121.79,
        90
      ],
      "FEED_TURN": [
        47.36,
        119.29,
        90
      ],
      "CATCH": [
        59,
        121,
        270
      ]
    },
    "pathEnds": {
      "to-feed-turn-1": "FEED_TURN",
      "to-feed-2": "FEED",
      "to-catch-3": "CATCH",
      "to-l-exit-back-4": "L_EXIT_BACK",
      "to-r-exit-back-5": "R_EXIT_BACK",
      "to-r-home-6": "R_HOME",
      "to-garden-in-7": "GARDEN_IN",
      "to-garden-8": "GARDEN",
      "to-r-back-9": "R_BACK",
      "to-r-back-10": "R_BACK",
      "to-park-11": "PARK"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-25",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "p-1",
        "kind": "path",
        "lineId": "to-feed-turn-1",
        "park": false
      },
      {
        "id": "p-2",
        "kind": "path",
        "lineId": "to-feed-2",
        "park": false
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "Left CELL up (the partner's TIP 1)?",
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
        "id": "a-5",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "w-6",
        "kind": "firstOf",
        "label": "Fire and feed (TIP 2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "a-7",
        "kind": "action",
        "name": "StreamOff"
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-catch-3",
        "park": false
      },
      {
        "id": "w-9",
        "kind": "firstOf",
        "label": "TIP 2: catch the spill",
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
        "id": "p-10",
        "kind": "path",
        "lineId": "to-l-exit-back-4",
        "park": false
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-r-exit-back-5",
        "park": false
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-r-home-6",
        "park": false
      },
      {
        "id": "w-13",
        "kind": "firstOf",
        "label": "Fire at the right CELL",
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
        "id": "w-23",
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
            "afterMs": 1000,
            "cards": [
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
                "id": "p-17",
                "kind": "path",
                "lineId": "to-r-back-9",
                "park": false
              },
              {
                "id": "w-18",
                "kind": "firstOf",
                "label": "Fire the GARDEN (TIP 3)",
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
            "label": "No: the GARDEN"
          }
        ]
      },
      {
        "id": "p-24",
        "kind": "path",
        "lineId": "to-park-11",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}