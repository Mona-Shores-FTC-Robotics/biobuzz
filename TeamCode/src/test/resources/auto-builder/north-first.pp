{
  "startPoint": {
    "x": 59,
    "y": 132.25,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-n-exit-1",
      "color": "#3cc8e4",
      "name": "START to N_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 108
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-exit-2",
      "color": "#3cc8e4",
      "name": "N_EXIT to S_EXIT",
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
      "id": "to-s-home-3",
      "color": "#3cc8e4",
      "name": "S_EXIT to S_HOME",
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
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "to-n-exit-4",
      "color": "#3cc8e4",
      "name": "S_HOME to N_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 108
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-home-5",
      "color": "#3cc8e4",
      "name": "N_EXIT to N_HOME",
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
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-far-flower-6",
      "color": "#3cc8e4",
      "name": "N_HOME to FAR_FLOWER",
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
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "to-n-home-7",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to N_HOME",
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
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-park-8",
      "color": "#3cc8e4",
      "name": "N_HOME to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 16,
        "y": 120
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 120
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "to-park2-9",
      "color": "#3cc8e4",
      "name": "PARK to PARK2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 16,
        "y": 122
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-far-flower-10",
      "color": "#3cc8e4",
      "name": "START to FAR_FLOWER",
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
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "to-n-home-11",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to N_HOME",
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
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-n-exit-12",
      "color": "#3cc8e4",
      "name": "N_HOME to N_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 108
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-exit-13",
      "color": "#3cc8e4",
      "name": "N_EXIT to S_EXIT",
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
      "id": "to-south-shot-14",
      "color": "#3cc8e4",
      "name": "S_EXIT to SOUTH_SHOT",
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
      "id": "to-south-plunge-in-15",
      "color": "#3cc8e4",
      "name": "SOUTH_SHOT to SOUTH_PLUNGE_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 49,
        "endDeg": 270
      }
    },
    {
      "id": "to-south-plunge-16",
      "color": "#3cc8e4",
      "name": "SOUTH_PLUNGE_IN to SOUTH_PLUNGE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-south-shot-17",
      "color": "#3cc8e4",
      "name": "SOUTH_PLUNGE to SOUTH_SHOT",
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
      "id": "to-garden-18",
      "color": "#3cc8e4",
      "name": "SOUTH_SHOT to GARDEN",
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
        "startDeg": 49,
        "endDeg": 270
      }
    },
    {
      "id": "to-south-shot-19",
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
      "id": "to-park-20",
      "color": "#3cc8e4",
      "name": "SOUTH_SHOT to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 16,
        "y": 120
      },
      "controlPoints": [
        {
          "x": 34,
          "y": 60
        },
        {
          "x": 34,
          "y": 126
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park2-21",
      "color": "#3cc8e4",
      "name": "PARK to PARK2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 16,
        "y": 122
      },
      "controlPoints": [],
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
      "lineId": "to-n-exit-1"
    },
    {
      "kind": "path",
      "lineId": "to-s-exit-2"
    },
    {
      "kind": "path",
      "lineId": "to-s-home-3"
    },
    {
      "kind": "path",
      "lineId": "to-n-exit-4"
    },
    {
      "kind": "path",
      "lineId": "to-n-home-5"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-6"
    },
    {
      "kind": "path",
      "lineId": "to-n-home-7"
    },
    {
      "kind": "path",
      "lineId": "to-park-8"
    },
    {
      "kind": "path",
      "lineId": "to-park2-9"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-10"
    },
    {
      "kind": "path",
      "lineId": "to-n-home-11"
    },
    {
      "kind": "path",
      "lineId": "to-n-exit-12"
    },
    {
      "kind": "path",
      "lineId": "to-s-exit-13"
    },
    {
      "kind": "path",
      "lineId": "to-south-shot-14"
    },
    {
      "kind": "path",
      "lineId": "to-south-plunge-in-15"
    },
    {
      "kind": "path",
      "lineId": "to-south-plunge-16"
    },
    {
      "kind": "path",
      "lineId": "to-south-shot-17"
    },
    {
      "kind": "path",
      "lineId": "to-garden-18"
    },
    {
      "kind": "path",
      "lineId": "to-south-shot-19"
    },
    {
      "kind": "path",
      "lineId": "to-park-20"
    },
    {
      "kind": "path",
      "lineId": "to-park2-21"
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
    "exportName": "north-first",
    "registry": {
      "actions": [
        "LaunchAll",
        "SpinUp"
      ],
      "conditions": [
        "LeftCellUp",
        "IntakeFull",
        "Empty",
        "RightCellUp"
      ],
      "typicalS": {
        "LaunchAll": 2.0,
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
      "N_HOME": [
        59,
        131.75,
        270
      ],
      "FAR_FLOWER": [
        47.4,
        130.5,
        90
      ],
      "N_EXIT": [
        57.5,
        108,
        270
      ],
      "S_EXIT": [
        57.5,
        34,
        270
      ],
      "S_HOME": [
        57.5,
        10,
        90
      ],
      "SOUTH_SHOT": [
        36,
        30,
        49
      ],
      "SOUTH_PLUNGE_IN": [
        55,
        24,
        270
      ],
      "SOUTH_PLUNGE": [
        55,
        10.5,
        270
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "PARK": [
        16,
        120,
        90
      ],
      "PARK2": [
        16,
        122,
        90
      ]
    },
    "pathEnds": {
      "to-n-exit-1": "N_EXIT",
      "to-s-exit-2": "S_EXIT",
      "to-s-home-3": "S_HOME",
      "to-n-exit-4": "N_EXIT",
      "to-n-home-5": "N_HOME",
      "to-far-flower-6": "FAR_FLOWER",
      "to-n-home-7": "N_HOME",
      "to-park-8": "PARK",
      "to-park2-9": "PARK2",
      "to-far-flower-10": "FAR_FLOWER",
      "to-n-home-11": "N_HOME",
      "to-n-exit-12": "N_EXIT",
      "to-s-exit-13": "S_EXIT",
      "to-south-shot-14": "SOUTH_SHOT",
      "to-south-plunge-in-15": "SOUTH_PLUNGE_IN",
      "to-south-plunge-16": "SOUTH_PLUNGE",
      "to-south-shot-17": "SOUTH_SHOT",
      "to-garden-18": "GARDEN",
      "to-south-shot-19": "SOUTH_SHOT",
      "to-park-20": "PARK",
      "to-park2-21": "PARK2"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-37",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "w-38",
        "kind": "firstOf",
        "label": "North CELL up (partner's TIP 1)?",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "w-15",
                "kind": "firstOf",
                "label": "Fire the preloads",
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
                "id": "p-16",
                "kind": "path",
                "lineId": "to-far-flower-10",
                "park": false
              },
              {
                "id": "w-17",
                "kind": "firstOf",
                "label": "Collect at the FLOWER",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1800,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-18",
                "kind": "path",
                "lineId": "to-n-home-11",
                "park": false
              },
              {
                "id": "w-19",
                "kind": "firstOf",
                "label": "Fire until it tips (TIP 2)",
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
              },
              {
                "id": "w-20",
                "kind": "firstOf",
                "label": "Catch the TIP 2 spill",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2200,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-21",
                "kind": "path",
                "lineId": "to-n-exit-12",
                "park": false
              },
              {
                "id": "p-22",
                "kind": "path",
                "lineId": "to-s-exit-13",
                "park": false
              },
              {
                "id": "p-23",
                "kind": "path",
                "lineId": "to-south-shot-14",
                "park": false
              },
              {
                "id": "a-24",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "p-25",
                "kind": "path",
                "lineId": "to-south-plunge-in-15",
                "park": false
              },
              {
                "id": "p-26",
                "kind": "path",
                "lineId": "to-south-plunge-16",
                "park": false
              },
              {
                "id": "w-27",
                "kind": "firstOf",
                "label": "The TIP 1 spill at the wall",
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
                "id": "p-28",
                "kind": "path",
                "lineId": "to-south-shot-17",
                "park": false
              },
              {
                "id": "w-29",
                "kind": "firstOf",
                "label": "Fire (TIP 3)",
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
                "id": "w-34",
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
                        "id": "p-30",
                        "kind": "path",
                        "lineId": "to-garden-18",
                        "park": false
                      },
                      {
                        "id": "w-31",
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
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "p-32",
                        "kind": "path",
                        "lineId": "to-south-shot-19",
                        "park": false
                      },
                      {
                        "id": "w-33",
                        "kind": "firstOf",
                        "label": "Fire until it tips (TIP 3)",
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
                "id": "p-35",
                "kind": "path",
                "lineId": "to-park-20",
                "park": false
              },
              {
                "id": "p-36",
                "kind": "path",
                "lineId": "to-park2-21",
                "park": true
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 6000,
            "cards": [
              {
                "id": "p-1",
                "kind": "path",
                "lineId": "to-n-exit-1",
                "park": false
              },
              {
                "id": "p-2",
                "kind": "path",
                "lineId": "to-s-exit-2",
                "park": false
              },
              {
                "id": "p-3",
                "kind": "path",
                "lineId": "to-s-home-3",
                "park": false
              },
              {
                "id": "w-4",
                "kind": "firstOf",
                "label": "Fire until it tips (our TIP 1)",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
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
                "id": "w-5",
                "kind": "firstOf",
                "label": "Catch the TIP 1 spill",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2200,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-6",
                "kind": "path",
                "lineId": "to-n-exit-4",
                "park": false
              },
              {
                "id": "p-7",
                "kind": "path",
                "lineId": "to-n-home-5",
                "park": false
              },
              {
                "id": "w-8",
                "kind": "firstOf",
                "label": "Fire at the north CELL",
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
                "lineId": "to-far-flower-6",
                "park": false
              },
              {
                "id": "w-10",
                "kind": "firstOf",
                "label": "Collect at the FLOWER (fallback)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1800,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-11",
                "kind": "path",
                "lineId": "to-n-home-7",
                "park": false
              },
              {
                "id": "w-12",
                "kind": "firstOf",
                "label": "Fire until it tips (TIP 2, fallback)",
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
              },
              {
                "id": "p-13",
                "kind": "path",
                "lineId": "to-park-8",
                "park": false
              },
              {
                "id": "p-14",
                "kind": "path",
                "lineId": "to-park2-9",
                "park": true
              }
            ],
            "label": "No: the partner missed"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}