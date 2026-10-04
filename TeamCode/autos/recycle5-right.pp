{
  "startPoint": {
    "x": 61,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-catch-1",
      "color": "#3cc8e4",
      "name": "START to CATCH",
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
      "id": "to-shoot-r-2",
      "color": "#3cc8e4",
      "name": "CATCH to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
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
                "startDeg": 90,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-catch-back-3",
      "color": "#3cc8e4",
      "name": "CATCH to CATCH_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 25
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-shoot-r-4",
      "color": "#3cc8e4",
      "name": "CATCH_BACK to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
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
                "startDeg": 90,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-in-5",
      "color": "#3cc8e4",
      "name": "SHOOT_R to GARDEN_IN",
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
          "x": 52,
          "y": 26
        },
        {
          "x": 30,
          "y": 24
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
                "degrees": 82
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 82,
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
      "id": "to-garden-6",
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
      "id": "to-wait-7",
      "color": "#3cc8e4",
      "name": "GARDEN to WAIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 16
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 14
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
                "endDeg": 66
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 66
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-catch-8",
      "color": "#3cc8e4",
      "name": "WAIT to CATCH",
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
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 66,
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
      "id": "to-catch-back-9",
      "color": "#3cc8e4",
      "name": "CATCH to CATCH_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 25
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-shoot-r-10",
      "color": "#3cc8e4",
      "name": "CATCH_BACK to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
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
                "startDeg": 90,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-catch-11",
      "color": "#3cc8e4",
      "name": "SHOOT_R to CATCH",
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
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 82,
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
      "id": "to-catch-back-12",
      "color": "#3cc8e4",
      "name": "CATCH to CATCH_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 25
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-shoot-r-13",
      "color": "#3cc8e4",
      "name": "CATCH_BACK to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
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
                "startDeg": 90,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
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
      "lineId": "to-catch-1"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-2"
    },
    {
      "kind": "path",
      "lineId": "to-catch-back-3"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-4"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-5"
    },
    {
      "kind": "path",
      "lineId": "to-garden-6"
    },
    {
      "kind": "path",
      "lineId": "to-wait-7"
    },
    {
      "kind": "path",
      "lineId": "to-catch-8"
    },
    {
      "kind": "path",
      "lineId": "to-catch-back-9"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-10"
    },
    {
      "kind": "path",
      "lineId": "to-catch-11"
    },
    {
      "kind": "path",
      "lineId": "to-catch-back-12"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-13"
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
    "exportName": "recycle5-right",
    "registry": {
      "actions": [
        "LaunchAll",
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
        "CollectSeen": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        61,
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
      "WAIT": [
        40,
        16,
        66
      ],
      "CATCH": [
        57.5,
        28,
        90
      ],
      "CATCH_BACK": [
        57.5,
        25,
        90
      ],
      "SHOOT_R": [
        50,
        18,
        82
      ]
    },
    "pathEnds": {
      "to-catch-1": "CATCH",
      "to-shoot-r-2": "SHOOT_R",
      "to-catch-back-3": "CATCH_BACK",
      "to-shoot-r-4": "SHOOT_R",
      "to-garden-in-5": "GARDEN_IN",
      "to-garden-6": "GARDEN",
      "to-wait-7": "WAIT",
      "to-catch-8": "CATCH",
      "to-catch-back-9": "CATCH_BACK",
      "to-shoot-r-10": "SHOOT_R",
      "to-catch-11": "CATCH",
      "to-catch-back-12": "CATCH_BACK",
      "to-shoot-r-13": "SHOOT_R"
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
        "id": "p-2",
        "kind": "path",
        "lineId": "to-catch-1",
        "park": false
      },
      {
        "id": "w-3",
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
        "id": "w-4",
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
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "CollectSeen"
      },
      {
        "id": "w-9",
        "kind": "firstOf",
        "label": "Caught 4?",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": [
              {
                "id": "p-5",
                "kind": "path",
                "lineId": "to-shoot-r-2",
                "park": false
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 200,
            "cards": [
              {
                "id": "w-6",
                "kind": "firstOf",
                "label": "Top up (3)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2000,
                    "cards": []
                  }
                ],
                "alongside": "CollectSeen"
              },
              {
                "id": "p-7",
                "kind": "path",
                "lineId": "to-catch-back-3",
                "park": false
              },
              {
                "id": "p-8",
                "kind": "path",
                "lineId": "to-shoot-r-4",
                "park": false
              }
            ],
            "label": "No: look"
          }
        ]
      },
      {
        "id": "w-10",
        "kind": "firstOf",
        "label": "Our CELL up (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 14000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-11",
        "kind": "firstOf",
        "label": "Fire the spill (3)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 600,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-garden-in-5",
        "park": false
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-garden-6",
        "park": false
      },
      {
        "id": "w-14",
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
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-15",
        "kind": "path",
        "lineId": "to-wait-7",
        "park": false
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "Fire the GARDEN (TIP 3)",
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
        "id": "p-17",
        "kind": "path",
        "lineId": "to-catch-8",
        "park": false
      },
      {
        "id": "w-18",
        "kind": "firstOf",
        "label": "Catch TIP 3's spill",
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
        ],
        "alongside": "CollectSeen"
      },
      {
        "id": "p-19",
        "kind": "path",
        "lineId": "to-catch-back-9",
        "park": false
      },
      {
        "id": "p-20",
        "kind": "path",
        "lineId": "to-shoot-r-10",
        "park": false
      },
      {
        "id": "w-21",
        "kind": "firstOf",
        "label": "Our CELL up (5)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 14000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-22",
        "kind": "firstOf",
        "label": "Fire (5)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 400,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-23",
        "kind": "path",
        "lineId": "to-catch-11",
        "park": false
      },
      {
        "id": "w-24",
        "kind": "firstOf",
        "label": "Pick up the rest (5)",
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
        "id": "p-25",
        "kind": "path",
        "lineId": "to-catch-back-12",
        "park": false
      },
      {
        "id": "p-26",
        "kind": "path",
        "lineId": "to-shoot-r-13",
        "park": false
      },
      {
        "id": "w-27",
        "kind": "firstOf",
        "label": "Fire again (TIP 5)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-30",
        "kind": "firstOf",
        "label": "Tipped? (5)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [],
            "label": "Yes"
          },
          {
            "afterMs": 50,
            "cards": [
              {
                "id": "w-28",
                "kind": "firstOf",
                "label": "Pick up more (5.1)",
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
                ],
                "alongside": "CollectSeen"
              },
              {
                "id": "w-29",
                "kind": "firstOf",
                "label": "Fire once more (5.1)",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1200,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              }
            ],
            "label": "No: more"
          }
        ]
      },
      {
        "id": "w-33",
        "kind": "firstOf",
        "label": "Tipped? (5)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [],
            "label": "Yes"
          },
          {
            "afterMs": 50,
            "cards": [
              {
                "id": "w-31",
                "kind": "firstOf",
                "label": "Pick up more (5.2)",
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
                ],
                "alongside": "CollectSeen"
              },
              {
                "id": "w-32",
                "kind": "firstOf",
                "label": "Fire once more (5.2)",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1200,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              }
            ],
            "label": "No: more"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}