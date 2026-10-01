{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-l-exit-1",
      "color": "#3cc8e4",
      "name": "START to L_EXIT",
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
        "degrees": 90
      }
    },
    {
      "id": "to-feed-in-2",
      "color": "#3cc8e4",
      "name": "L_EXIT to FEED_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 121.79
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-feed-3",
      "color": "#3cc8e4",
      "name": "FEED_IN to FEED",
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
      "id": "to-catch-4",
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
      "id": "to-l-exit-back-5",
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
      "id": "to-r-exit-back-6",
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
      "id": "to-r-home-7",
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
      "id": "to-l-exit-8",
      "color": "#3cc8e4",
      "name": "R_HOME to L_EXIT",
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
        "degrees": 90
      }
    },
    {
      "id": "to-l-home-9",
      "color": "#3cc8e4",
      "name": "L_EXIT to L_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.4,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.4,
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
      "id": "to-l-back-10",
      "color": "#3cc8e4",
      "name": "L_HOME to L_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 131
      },
      "controlPoints": [],
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
      "lineId": "to-l-exit-1"
    },
    {
      "kind": "path",
      "lineId": "to-feed-in-2"
    },
    {
      "kind": "path",
      "lineId": "to-feed-3"
    },
    {
      "kind": "path",
      "lineId": "to-catch-4"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-back-5"
    },
    {
      "kind": "path",
      "lineId": "to-r-exit-back-6"
    },
    {
      "kind": "path",
      "lineId": "to-r-home-7"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-8"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-9"
    },
    {
      "kind": "path",
      "lineId": "to-l-back-10"
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
    "exportName": "four-tip-x",
    "registry": {
      "actions": [
        "LaunchAll",
        "StreamOn",
        "StreamOff",
        "CollectSeen"
      ],
      "conditions": [
        "Empty",
        "Tip",
        "IntakeFull",
        "RightCellUp",
        "LeftCellUp"
      ],
      "typicalS": {
        "LaunchAll": 2.0,
        "StreamOn": 1.0,
        "StreamOff": 1.0,
        "CollectSeen": 2.0
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
      "R_HOME": [
        57.5,
        10,
        90
      ],
      "L_HOME": [
        59,
        131.75,
        270
      ],
      "L_BACK": [
        59,
        131,
        270
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
      "R_EXIT": [
        57.5,
        34,
        90
      ],
      "R_EXIT_BACK": [
        57.5,
        34,
        270
      ],
      "CATCH": [
        59,
        121,
        270
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
      ]
    },
    "pathEnds": {
      "to-l-exit-1": "L_EXIT",
      "to-feed-in-2": "FEED_IN",
      "to-feed-3": "FEED",
      "to-catch-4": "CATCH",
      "to-l-exit-back-5": "L_EXIT_BACK",
      "to-r-exit-back-6": "R_EXIT_BACK",
      "to-r-home-7": "R_HOME",
      "to-l-exit-8": "L_EXIT",
      "to-l-home-9": "L_HOME",
      "to-l-back-10": "L_BACK"
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
        "id": "w-2",
        "kind": "firstOf",
        "label": "TIP 1?",
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
        ]
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "TIP 1: catch the spill",
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
        "id": "p-4",
        "kind": "path",
        "lineId": "to-l-exit-1",
        "park": false
      },
      {
        "id": "p-5",
        "kind": "path",
        "lineId": "to-feed-in-2",
        "park": false
      },
      {
        "id": "p-6",
        "kind": "path",
        "lineId": "to-feed-3",
        "park": false
      },
      {
        "id": "a-8",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "w-9",
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
        "id": "a-10",
        "kind": "action",
        "name": "StreamOff"
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-catch-4",
        "park": false
      },
      {
        "id": "w-12",
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
        "id": "p-13",
        "kind": "path",
        "lineId": "to-l-exit-back-5",
        "park": false
      },
      {
        "id": "p-14",
        "kind": "path",
        "lineId": "to-r-exit-back-6",
        "park": false
      },
      {
        "id": "p-15",
        "kind": "path",
        "lineId": "to-r-home-7",
        "park": false
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "Fire (TIP 3)",
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
      },
      {
        "id": "w-17",
        "kind": "firstOf",
        "label": "TIP 3: catch the spill",
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
        "id": "p-18",
        "kind": "path",
        "lineId": "to-l-exit-8",
        "park": false
      },
      {
        "id": "p-19",
        "kind": "path",
        "lineId": "to-l-home-9",
        "park": false
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "Fire (TIP 4)",
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
        "id": "w-24",
        "kind": "firstOf",
        "label": "TIP 4 yet?",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [],
            "label": "Yes"
          },
          {
            "afterMs": 800,
            "cards": [
              {
                "id": "w-21",
                "kind": "firstOf",
                "label": "Leftovers (TIP 4)",
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
                ],
                "alongside": "CollectSeen"
              },
              {
                "id": "p-22",
                "kind": "path",
                "lineId": "to-l-back-10",
                "park": false
              },
              {
                "id": "w-23",
                "kind": "firstOf",
                "label": "Fire the leftovers (TIP 4)",
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
                ],
                "alongside": "LaunchAll"
              }
            ],
            "label": "No: the leftovers"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}